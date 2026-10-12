package config;

import exception.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Properties;

/**
 * Configuração da aplicação, lida do arquivo {@code ftcoin.properties} no classpath.
 * <p>
 * Qualquer chave pode ser sobrescrita na execução por uma propriedade de sistema de mesmo nome,
 * por exemplo {@code java -Doraculo.base=20 -jar ft-coin.jar}.
 * </p>
 */
public class Configuracao {

    private static final String ARQUIVO_PADRAO = "/ftcoin.properties";

    private final Properties propriedades;

    private Configuracao(Properties propriedades) {
        this.propriedades = propriedades;
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
        return new Configuracao(propriedades);
    }

    /**
     * Cria uma configuração a partir de propriedades já carregadas (útil em testes).
     *
     * @param propriedades pares chave/valor da configuração
     * @return a configuração
     */
    public static Configuracao de(Properties propriedades) {
        Properties copia = new Properties();
        copia.putAll(propriedades);
        return new Configuracao(copia);
    }

    /**
     * @param chave chave da configuração
     * @return o valor da chave, sem espaços nas pontas
     * @throws AppException se a chave não estiver configurada
     */
    public String getTexto(String chave) throws AppException {
        String valor = System.getProperty(chave, propriedades.getProperty(chave));
        if (valor == null || valor.isBlank()) {
            throw new AppException("Configuração obrigatória ausente: " + chave + ".");
        }
        return valor.trim();
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
