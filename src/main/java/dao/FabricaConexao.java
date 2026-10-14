package dao;

import config.Configuracao;
import config.Mensagens;
import exception.AppException;
import exception.PersistenciaException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Conexões JDBC com o MariaDB. Cada operação abre e fecha a própria conexão, o que basta para uma
 * aplicação de console com um usuário por vez.
 */
final class FabricaConexao {

    private static final String SCRIPT_SCHEMA = "/db/schema.sql";

    static {
        // Os erros já chegam ao usuário pela PersistenciaException; o log do driver só duplicaria.
        if (System.getProperty("mariadb.logging.disable") == null) {
            System.setProperty("mariadb.logging.disable", "true");
        }
    }

    /** Dados de acesso ao banco, lidos das chaves {@code db.*} da configuração. */
    record ConfiguracaoBanco(String url, String usuario, String senha, int timeoutSegundos) {

        private static final int TIMEOUT_PADRAO = 5;

        static ConfiguracaoBanco carregar(Configuracao configuracao) throws AppException {
            String timeout = configuracao.getTexto("db.timeout", String.valueOf(TIMEOUT_PADRAO));
            try {
                return new ConfiguracaoBanco(
                        configuracao.getTexto("db.url"),
                        configuracao.getTexto("db.usuario"),
                        configuracao.getTexto("db.senha", ""),
                        Integer.parseInt(timeout));
            } catch (NumberFormatException _) {
                throw new AppException("erro.config.inteiro", "db.timeout", timeout);
            }
        }

        /** Omite a senha. */
        @Override
        public String toString() {
            return "ConfiguracaoBanco[url=" + url + ", usuario=" + usuario + "]";
        }
    }

    @FunctionalInterface
    interface OperacaoComConexao<T> {
        T executar(Connection conexao) throws SQLException;
    }

    private final ConfiguracaoBanco configuracao;

    FabricaConexao(ConfiguracaoBanco configuracao) {
        this.configuracao = configuracao;
    }

    ConfiguracaoBanco configuracao() {
        return configuracao;
    }

    /** Quem chama é responsável por fechar a conexão. */
    Connection abrir() throws PersistenciaException {
        Properties propriedades = new Properties();
        propriedades.setProperty("user", configuracao.usuario());
        propriedades.setProperty("password", configuracao.senha());
        propriedades.setProperty("connectTimeout", String.valueOf(configuracao.timeoutSegundos() * 1000));
        try {
            return DriverManager.getConnection(configuracao.url(), propriedades);
        } catch (SQLException e) {
            throw new PersistenciaException(e, "banco.semConexao", configuracao.url(), configuracao.usuario(),
                    e.getMessage());
        }
    }

    void testarConexao() throws PersistenciaException {
        executar("banco.testarConexao", conexao -> {
            if (!conexao.isValid(configuracao.timeoutSegundos())) {
                throw new SQLException(Mensagens.get("banco.semResposta", configuracao.timeoutSegundos()));
            }
            return null;
        });
    }

    /**
     * @param chaveErro mensagem usada se o banco falhar, com o erro do banco como argumento
     *                  (ex.: {@code banco.incluirCarteira})
     */
    <T> T executar(String chaveErro, OperacaoComConexao<T> operacao) throws PersistenciaException {
        try (Connection conexao = abrir()) {
            return operacao.executar(conexao);
        } catch (SQLException e) {
            throw new PersistenciaException(e, chaveErro, e.getMessage());
        }
    }

    /** Confirma tudo ao final ou desfaz tudo se algum comando falhar. */
    <T> T executarEmTransacao(String chaveErro, OperacaoComConexao<T> operacao) throws PersistenciaException {
        return executar(chaveErro, conexao -> {
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

    /** Testa a conexão e cria as tabelas que faltarem; pode rodar a cada inicialização. */
    void criarTabelas() throws AppException {
        testarConexao();
        List<String> comandos = comandos(lerScript());
        executarEmTransacao("banco.criarTabelas", conexao -> {
            try (Statement statement = conexao.createStatement()) {
                for (String comando : comandos) {
                    statement.execute(comando);
                }
            }
            return null;
        });
    }

    private static String lerScript() throws AppException {
        try (InputStream in = FabricaConexao.class.getResourceAsStream(SCRIPT_SCHEMA)) {
            if (in == null) {
                throw new AppException("erro.script.naoEncontrado", SCRIPT_SCHEMA);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AppException(e, "erro.script.leitura", SCRIPT_SCHEMA, e.getMessage());
        }
    }

    /**
     * Separa o script em comandos pelo {@code ;}, ignorando comentários {@code --}.
     * Por isso o script não pode ter {@code ;} dentro de textos.
     */
    static List<String> comandos(String script) {
        StringBuilder semComentarios = new StringBuilder();
        for (String linha : script.split("\\R")) {
            if (!linha.trim().startsWith("--")) {
                semComentarios.append(linha).append('\n');
            }
        }
        List<String> comandos = new ArrayList<>();
        for (String comando : semComentarios.toString().split(";")) {
            if (!comando.isBlank()) {
                comandos.add(comando.trim());
            }
        }
        return comandos;
    }
}
