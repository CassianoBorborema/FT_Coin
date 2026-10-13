package infra.db;

import exception.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Cria as tabelas da aplicação a partir do script {@value #SCRIPT_SCHEMA} do classpath.
 * O script usa {@code CREATE TABLE IF NOT EXISTS}, então pode ser executado a cada inicialização.
 */
public class InicializadorBanco {

    private static final String SCRIPT_SCHEMA = "/db/schema.sql";

    private final FabricaConexao fabricaConexao;

    public InicializadorBanco(FabricaConexao fabricaConexao) {
        this.fabricaConexao = fabricaConexao;
    }

    /**
     * Testa a conexão e cria as tabelas que ainda não existem.
     *
     * @throws AppException se o banco estiver inacessível ou o script falhar
     */
    public void inicializar() throws AppException {
        fabricaConexao.testarConexao();
        List<String> comandos = comandos(lerScript());
        fabricaConexao.executarEmTransacao("criar as tabelas do banco", conexao -> {
            try (Statement statement = conexao.createStatement()) {
                for (String comando : comandos) {
                    statement.execute(comando);
                }
            }
            return null;
        });
    }

    private String lerScript() throws AppException {
        try (InputStream in = getClass().getResourceAsStream(SCRIPT_SCHEMA)) {
            if (in == null) {
                throw new AppException("Script do banco não encontrado: " + SCRIPT_SCHEMA + ".");
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new AppException("Erro ao ler o script do banco " + SCRIPT_SCHEMA + ": " + e.getMessage(), e);
        }
    }

    /**
     * Separa o script em comandos, ignorando comentários de linha ({@code --}).
     * O script não pode ter {@code ;} dentro de textos.
     *
     * @param script conteúdo do script SQL
     * @return os comandos, sem o {@code ;} final
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
