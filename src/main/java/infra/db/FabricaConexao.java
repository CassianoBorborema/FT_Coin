package infra.db;

import exception.PersistenciaException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexões JDBC com o banco. Cada operação abre e fecha a própria conexão, o que basta para uma
 * aplicação de console com um usuário por vez.
 */
public class FabricaConexao {

    static {
        // Os erros já chegam ao usuário pela PersistenciaException; o log do driver só duplicaria.
        if (System.getProperty("mariadb.logging.disable") == null) {
            System.setProperty("mariadb.logging.disable", "true");
        }
    }

    private final ConfiguracaoBanco configuracao;

    public FabricaConexao(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
    }

    @FunctionalInterface
    public interface OperacaoComConexao<T> {
        T executar(Connection conexao) throws SQLException;
    }

    /** Quem chama é responsável por fechar a conexão. */
    public Connection abrir() throws PersistenciaException {
        Properties propriedades = new Properties();
        propriedades.setProperty("user", configuracao.usuario());
        propriedades.setProperty("password", configuracao.senha());
        propriedades.setProperty("connectTimeout", String.valueOf(configuracao.timeoutSegundos() * 1000));
        try {
            return DriverManager.getConnection(configuracao.url(), propriedades);
        } catch (SQLException e) {
            throw new PersistenciaException("Não foi possível conectar ao banco " + configuracao.url()
                    + " com o usuário " + configuracao.usuario() + ": " + e.getMessage(), e);
        }
    }

    public void testarConexao() throws PersistenciaException {
        executar("testar a conexão", conexao -> {
            if (!conexao.isValid(configuracao.timeoutSegundos())) {
                throw new SQLException("o banco não respondeu em " + configuracao.timeoutSegundos() + "s");
            }
            return null;
        });
    }

    /**
     * @param descricao usada na mensagem de erro, por exemplo "incluir a carteira"
     */
    public <T> T executar(String descricao, OperacaoComConexao<T> operacao) throws PersistenciaException {
        try (Connection conexao = abrir()) {
            return operacao.executar(conexao);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao " + descricao + ": " + e.getMessage(), e);
        }
    }

    /** Confirma tudo ao final ou desfaz tudo se algum comando falhar. */
    public <T> T executarEmTransacao(String descricao, OperacaoComConexao<T> operacao)
            throws PersistenciaException {
        return executar(descricao, conexao -> {
            conexao.setAutoCommit(false);
            try {
                T resultado = operacao.executar(conexao);
                conexao.commit();
                return resultado;
            } catch (SQLException | RuntimeException e) {
                conexao.rollback();
                throw e;
            }
        });
    }
}
