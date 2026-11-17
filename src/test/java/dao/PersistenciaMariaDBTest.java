package dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import dto.OraculoDTO;
import exception.AppException;
import exception.PersistenciaException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

/**
 * Roda o contrato dos DAOs contra um MariaDB real em contêiner. Sem Docker, os testes são ignorados.
 */
@Testcontainers(disabledWithoutDocker = true)
class PersistenciaMariaDBTest extends PersistenciaContrato {

    @Container
    private static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.4");

    private static FabricaConexao conexoes;

    @BeforeAll
    static void conectar() {
        conexoes = new FabricaConexao(new FabricaConexao.ConfiguracaoBanco(
                MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword(), 5));
    }

    @Override
    protected Persistencia criarPersistenciaVazia() throws AppException {
        Persistencia persistencia = Persistencia.mariaDB(conexoes);
        conexoes.executar("limpar as tabelas", conexao -> {
            try (Statement statement = conexao.createStatement()) {
                statement.execute("DELETE FROM MOVIMENTACAO");
                statement.execute("DELETE FROM CARTEIRA");
                statement.execute("DELETE FROM COTACAO");
            }
            return null;
        });
        return persistencia;
    }

    @Test
    void criarTabelasDeNovoNaoApagaDados() throws AppException {
        Persistencia persistencia = criarPersistenciaVazia();
        CarteiraDTO carteira = new CarteiraDTO(0, "Ana", "XP");
        persistencia.carteiras().incluir(carteira);

        assertDoesNotThrow(conexoes::criarTabelas);
        assertDoesNotThrow(() -> persistencia.carteiras().consultarPorId(carteira.getIdentificador()));
    }

    @Test
    void bancoComEsquemaOficialPreexistenteSoGanhaCotacao() throws AppException {
        conexoes.executar("recriar o esquema oficial", conexao -> {
            try (Statement statement = conexao.createStatement()) {
                statement.execute("DROP TABLE IF EXISTS MOVIMENTACAO");
                statement.execute("DROP TABLE IF EXISTS CARTEIRA");
                statement.execute("DROP TABLE IF EXISTS COTACAO");
                statement.execute("CREATE TABLE CARTEIRA (IdCarteira INT NOT NULL AUTO_INCREMENT, "
                        + "Titular VARCHAR(50) NOT NULL, Corretora VARCHAR(50) NOT NULL, PRIMARY KEY (IdCarteira)) "
                        + "ENGINE = InnoDB DEFAULT CHARSET = latin1");
                statement.execute("CREATE TABLE MOVIMENTACAO (IdCarteira INT NOT NULL, "
                        + "IdMovimento INT NOT NULL AUTO_INCREMENT, Data DATE DEFAULT NULL, "
                        + "TipoOperacao CHAR(1) DEFAULT NULL, Quantidade DECIMAL(10,3) DEFAULT NULL, "
                        + "PRIMARY KEY (IdMovimento), KEY IdCarteira (IdCarteira), "
                        + "CONSTRAINT MOVIMENTACAO_ibfk_1 FOREIGN KEY (IdCarteira) REFERENCES CARTEIRA (IdCarteira)) "
                        + "ENGINE = InnoDB DEFAULT CHARSET = latin1");
            }
            return null;
        });

        Persistencia persistencia = Persistencia.mariaDB(conexoes);
        CarteiraDTO carteira = new CarteiraDTO(0, "Ana", "XP");
        persistencia.carteiras().incluir(carteira);
        persistencia.movimentacoes().incluir(new MovimentacaoDTO(0, carteira.getIdentificador(),
                LocalDate.of(2026, 1, 1), TipoMovimentacao.COMPRA, 1.5));
        persistencia.cotacoes().incluir(new OraculoDTO(LocalDate.of(2026, 1, 1), 12.34));

        assertEquals(List.of("CARTEIRA", "COTACAO", "MOVIMENTACAO"), tabelasDoBanco());
        assertEquals(1.5, persistencia.movimentacoes().listarPorCarteira(carteira.getIdentificador()).get(0)
                .getQuantidade());
        assertEquals(12.34, persistencia.cotacoes().consultarPorData(LocalDate.of(2026, 1, 1)).getCotacao());
    }

    @Test
    void bancoRecusaMovimentacaoDeCarteiraInexistente() throws AppException {
        Persistencia persistencia = criarPersistenciaVazia();

        assertThrows(PersistenciaException.class, () -> persistencia.movimentacoes().incluir(
                new MovimentacaoDTO(0, 999, LocalDate.of(2026, 1, 1), TipoMovimentacao.COMPRA, 1)));
    }

    @Test
    void bancoRecusaExcluirCarteiraComMovimentacoes() throws AppException {
        Persistencia persistencia = criarPersistenciaVazia();
        CarteiraDTO carteira = new CarteiraDTO(0, "Ana", "XP");
        persistencia.carteiras().incluir(carteira);
        persistencia.movimentacoes().incluir(new MovimentacaoDTO(0, carteira.getIdentificador(),
                LocalDate.of(2026, 1, 1), TipoMovimentacao.COMPRA, 1));

        assertThrows(PersistenciaException.class,
                () -> persistencia.carteiras().excluir(carteira.getIdentificador()));
    }

    private static List<String> tabelasDoBanco() throws AppException {
        return conexoes.executar("listar as tabelas", conexao -> {
            try (Statement statement = conexao.createStatement();
                 ResultSet rs = statement.executeQuery("SELECT table_name FROM information_schema.tables "
                         + "WHERE table_schema = DATABASE() ORDER BY table_name")) {
                List<String> nomes = new ArrayList<>();
                while (rs.next()) {
                    nomes.add(rs.getString(1));
                }
                return nomes;
            }
        });
    }
}
