package config;

import exception.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Properties;
import java.util.function.UnaryOperator;

/**
 * Configuração da aplicação. Cada chave é procurada, nesta ordem:
 * <ol>
 *   <li>propriedade de sistema de mesmo nome ({@code -Ddb.senha=...});</li>
 *   <li>variável de ambiente {@code FTCOIN_} + chave em maiúsculas com {@code _} no lugar de
 *       {@code .} (ex.: {@code db.senha} → {@code FTCOIN_DB_SENHA});</li>
 *   <li>arquivo {@value #ARQUIVO_LOCAL} no diretório de execução, se existir (fora do Git,
 *       para senhas e ajustes da máquina);</li>
 *   <li>arquivo {@code ftcoin.properties} do classpath, com os valores padrão.</li>
 * </ol>
 */
public class Configuracao {

    private static final String ARQUIVO_PADRAO = "/ftcoin.properties";
    static final String ARQUIVO_LOCAL = "ftcoin.local.properties";
    private static final String PREFIXO_AMBIENTE = "FTCOIN_";

    private final Properties propriedades;
    private final UnaryOperator<String> ambiente;

    private Configuracao(Properties propriedades, UnaryOperator<String> ambiente) {
        this.propriedades = propriedades;
        this.ambiente = ambiente;
    }

    /**
     * Carrega a configuração padrão da aplicação.
     *
     * @return a configuração carregada
     * @throws AppException se o arquivo não existir ou não puder ser lido
     */
    public static Configuracao carregar() throws AppException {
        Properties propriedades = new Properties();
        try (InputStream in = Configuracao.class.getResourceAsStream(ARQUIVO_PADRAO)) {
            if (in == null) {
                throw new AppException("Arquivo de configuração não encontrado: " + ARQUIVO_PADRAO + ".");
            }
            propriedades.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new AppException("Erro ao ler a configuração " + ARQUIVO_PADRAO + ": " + e.getMessage());
        }
        carregarArquivoLocal(propriedades, Path.of(ARQUIVO_LOCAL));
        return new Configuracao(propriedades, System::getenv);
    }

    private static void carregarArquivoLocal(Properties propriedades, Path arquivo) throws AppException {
        if (!Files.isRegularFile(arquivo)) {
            return;
        }
        try (Reader leitor = Files.newBufferedReader(arquivo, StandardCharsets.UTF_8)) {
            propriedades.load(leitor);
        } catch (IOException e) {
            throw new AppException("Erro ao ler a configuração " + arquivo.toAbsolutePath() + ": " + e.getMessage());
        }
    }

    /**
     * Cria uma configuração a partir de propriedades já carregadas (útil em testes).
     *
     * @param propriedades pares chave/valor da configuração
     * @return a configuração
     */
    public static Configuracao de(Properties propriedades) {
        return de(propriedades, variavel -> null);
    }

    /**
     * Cria uma configuração com propriedades e variáveis de ambiente definidas pelo chamador
     * (útil em testes).
     *
     * @param propriedades pares chave/valor da configuração
     * @param ambiente     consulta de variáveis de ambiente pelo nome
     * @return a configuração
     */
    public static Configuracao de(Properties propriedades, UnaryOperator<String> ambiente) {
        Properties copia = new Properties();
        copia.putAll(propriedades);
        return new Configuracao(copia, ambiente);
    }

    /**
     * @param chave chave da configuração
     * @return o valor da chave, sem espaços nas pontas
     * @throws AppException se a chave não estiver configurada
     */
    public String getTexto(String chave) throws AppException {
        String valor = buscar(chave);
        if (valor == null || valor.isBlank()) {
            throw new AppException("Configuração obrigatória ausente: " + chave + ".");
        }
        return valor.trim();
    }

    /**
     * @param chave  chave da configuração
     * @param padrao valor usado se a chave não estiver configurada
     * @return o valor da chave (pode ser vazio) ou o padrão
     */
    public String getTexto(String chave, String padrao) {
        String valor = buscar(chave);
        return valor == null ? padrao : valor.trim();
    }

    private String buscar(String chave) {
        String valor = System.getProperty(chave);
        if (valor == null) {
            valor = ambiente.apply(nomeVariavelAmbiente(chave));
        }
        if (valor == null) {
            valor = propriedades.getProperty(chave);
        }
        return valor;
    }

    /**
     * @param chave chave da configuração, por exemplo {@code db.senha}
     * @return o nome da variável de ambiente equivalente, por exemplo {@code FTCOIN_DB_SENHA}
     */
    static String nomeVariavelAmbiente(String chave) {
        return PREFIXO_AMBIENTE + chave.replace('.', '_').toUpperCase(Locale.ROOT);
    }

    /**
     * @param chave chave da configuração
     * @return o valor da chave como número decimal (separador ponto)
     * @throws AppException se a chave não existir ou não for um número
     */
    public double getDecimal(String chave) throws AppException {
        String valor = getTexto(chave);
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            throw new AppException("Configuração " + chave + " deve ser um número: " + valor + ".");
        }
    }

    /**
     * @param chave chave da configuração
     * @return o valor da chave como número inteiro
     * @throws AppException se a chave não existir ou não for um inteiro
     */
    public long getInteiro(String chave) throws AppException {
        String valor = getTexto(chave);
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            throw new AppException("Configuração " + chave + " deve ser um número inteiro: " + valor + ".");
        }
    }

    /**
     * @param chave chave da configuração
     * @return o valor da chave como data no formato ISO ({@code yyyy-MM-dd})
     * @throws AppException se a chave não existir ou não for uma data válida
     */
    public LocalDate getData(String chave) throws AppException {
        String valor = getTexto(chave);
        try {
            return LocalDate.parse(valor);
        } catch (DateTimeParseException e) {
            throw new AppException("Configuração " + chave + " deve ser uma data no formato yyyy-MM-dd: " + valor + ".");
        }
    }
}
