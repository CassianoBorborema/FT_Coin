package infra.db;

import config.Configuracao;
import exception.AppException;

/**
 * Dados de acesso ao banco de dados.
 *
 * @param url            URL JDBC, por exemplo {@code jdbc:mariadb://localhost:3306/ftcoin}
 * @param usuario        usuário do banco
 * @param senha          senha do usuário (pode ser vazia)
 * @param timeoutSegundos tempo máximo para abrir uma conexão
 */
public record ConfiguracaoBanco(String url, String usuario, String senha, int timeoutSegundos) {

    private static final int TIMEOUT_PADRAO = 5;

    /** Não expõe a senha em logs e mensagens. */
    @Override
    public String toString() {
        return "ConfiguracaoBanco[url=" + url + ", usuario=" + usuario + "]";
    }

    /**
     * Lê as chaves {@code db.url}, {@code db.usuario}, {@code db.senha} e {@code db.timeout}.
     *
     * @param configuracao configuração da aplicação
     * @return os dados de acesso ao banco
     * @throws AppException se a URL ou o usuário não estiverem configurados
     */
    public static ConfiguracaoBanco carregar(Configuracao configuracao) throws AppException {
        String timeout = configuracao.getTexto("db.timeout", String.valueOf(TIMEOUT_PADRAO));
        int timeoutSegundos;
        try {
            timeoutSegundos = Integer.parseInt(timeout);
        } catch (NumberFormatException e) {
            throw new AppException("Configuração db.timeout deve ser um número inteiro: " + timeout + ".");
        }
        return new ConfiguracaoBanco(
                configuracao.getTexto("db.url"),
                configuracao.getTexto("db.usuario"),
                configuracao.getTexto("db.senha", ""),
                timeoutSegundos);
    }
}
