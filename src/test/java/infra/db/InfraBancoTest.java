package infra.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import config.Configuracao;
import exception.AppException;
import exception.PersistenciaException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class InfraBancoTest {

    @Test
    void scriptEhSeparadoEmComandosSemComentarios() {
        List<String> comandos = InicializadorBanco.comandos(
                "-- comentário; com ponto e vírgula\nCREATE TABLE a (x INT);\n\n  -- outro\nCREATE TABLE b (y INT);\n");

        assertEquals(List.of("CREATE TABLE a (x INT)", "CREATE TABLE b (y INT)"), comandos);
    }

    @Test
    void configuracaoDoBancoLeChavesEVariaveisDeAmbiente() throws AppException {
        Properties propriedades = new Properties();
        propriedades.setProperty("db.url", "jdbc:mariadb://localhost:3306/ftcoin");
        propriedades.setProperty("db.usuario", "ftcoin");
        Map<String, String> ambiente = Map.of("FTCOIN_DB_SENHA", "segredo", "FTCOIN_DB_USUARIO", "outro");

        ConfiguracaoBanco banco = ConfiguracaoBanco.carregar(Configuracao.de(propriedades, ambiente::get));

        assertEquals("jdbc:mariadb://localhost:3306/ftcoin", banco.url());
        assertEquals("outro", banco.usuario());
        assertEquals("segredo", banco.senha());
        assertEquals(5, banco.timeoutSegundos());
    }

    @Test
    void configuracaoDoBancoExigeUrl() {
        Properties propriedades = new Properties();
        propriedades.setProperty("db.usuario", "ftcoin");

        assertThrows(AppException.class, () -> ConfiguracaoBanco.carregar(Configuracao.de(propriedades)));
    }

    @Test
    void senhaNaoApareceNoToString() {
        ConfiguracaoBanco banco = new ConfiguracaoBanco("jdbc:mariadb://localhost/ftcoin", "ftcoin", "segredo", 5);

        assertFalse(banco.toString().contains("segredo"));
        assertTrue(banco.toString().contains("ftcoin"));
    }

    @Test
    void bancoInacessivelGeraPersistenciaException() {
        FabricaConexao conexoes = new FabricaConexao(
                new ConfiguracaoBanco("jdbc:mariadb://127.0.0.1:1/ftcoin", "ftcoin", "", 1));

        PersistenciaException erro = assertThrows(PersistenciaException.class, conexoes::testarConexao);
        assertTrue(erro.getMessage().contains("Não foi possível conectar"));
    }
}
