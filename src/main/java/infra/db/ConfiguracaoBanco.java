package infra.db;

import config.Configuracao;
import exception.AppException;

/**
 * Dados de acesso ao banco, lidos das chaves {@code db.*} da configuração.
 */
public record ConfiguracaoBanco(String url, String usuario, String senha, int timeoutSegundos) {

    private static final int TIMEOUT_PADRAO = 5;

    /** Omite a senha. */
    @Override
    public String toString() {
        return "ConfiguracaoBanco[url=" + url + ", usuario=" + usuario + "]";
    }

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
