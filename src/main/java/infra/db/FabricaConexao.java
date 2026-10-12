package infra.db;

import exception.PersistenciaException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Abre conexões JDBC com o banco de dados configurado.
 * <p>
 * Cada operação abre e fecha a própria conexão (com try-with-resources), o que basta para uma
 * aplicação de console com um usuário por vez.
 * </p>
 */
public class FabricaConexao {

    static {
        // O driver do MariaDB imprime seus próprios avisos no console; os erros já chegam ao
        // usuário pela PersistenciaException. Quem quiser os logs do driver pode rodar com
        // -Dmariadb.logging.disable=false.
        if (System.getProperty("mariadb.logging.disable") == null) {
            System.setProperty("mariadb.logging.disable", "true");
        }
    }

    private final ConfiguracaoBanco configuracao;

    public FabricaConexao(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
    }

    /** Operação executada com uma conexão aberta. */
    @FunctionalInterface
    public interface OperacaoComConexao<T> {
        T executar(Connection conexao) throws SQLException;
    }

    /**
     * Abre uma nova conexão. Quem chama é responsável por fechá-la.
     *
     * @return a conexão aberta
     * @throws PersistenciaException se não for possível conectar
     */
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

    /**
     * Confirma que o banco está acessível.
     *
     * @throws PersistenciaException se não for possível conectar ou a conexão não responder
     */
    public void testarConexao() throws PersistenciaException {
        executar("testar a conexão", conexao -> {
            if (!conexao.isValid(configuracao.timeoutSegundos())) {
                throw new SQLException("o banco não respondeu em " + configuracao.timeoutSegundos() + "s");
            }
            return null;
        });
    }

    /**
     * Executa uma operação com uma conexão própria, fechada ao final.
     *
     * @param descricao descrição da operação, usada na mensagem de erro
     * @param operacao  operação a executar
     * @return o resultado da operação
     * @throws PersistenciaException se a conexão ou a operação falharem
     */
    public <T> T executar(String descricao, OperacaoComConexao<T> operacao) throws PersistenciaException {
        try (Connection conexao = abrir()) {
            return operacao.executar(conexao);
        } catch (SQLException e) {
            throw new PersistenciaException("Erro ao " + descricao + ": " + e.getMessage(), e);
        }
    }

    /**
     * Executa uma operação dentro de uma transação: confirma se tudo der certo e desfaz
     * tudo se algum comando falhar.
     *
     * @param descricao descrição da operação, usada na mensagem de erro
     * @param operacao  operação a executar
     * @return o resultado da operação
     * @throws PersistenciaException se a conexão ou a operação falharem
     */
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
