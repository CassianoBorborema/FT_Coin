package dao;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import exception.PersistenciaException;
import java.sql.Statement;
import java.time.LocalDate;
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
                statement.execute("DELETE FROM movimentacao");
                statement.execute("DELETE FROM carteira");
                statement.execute("DELETE FROM cotacao");
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
    void bancoCriadoAntesDoCreditoPassaAAceitarCreditoESaque() throws AppException {
        Persistencia persistencia = criarPersistenciaVazia();
        CarteiraDTO carteira = new CarteiraDTO(0, "Ana", "XP");
        persistencia.carteiras().incluir(carteira);
        conexoes.executar("voltar à restrição antiga", conexao -> {
            try (Statement statement = conexao.createStatement()) {
                statement.execute("ALTER TABLE movimentacao DROP CONSTRAINT ck_movimentacao_tipo");
                statement.execute("ALTER TABLE movimentacao ADD CONSTRAINT ck_movimentacao_tipo CHECK (tipo IN ('C', 'V'))");
            }
            return null;
        });
        MovimentacaoDTO credito = new MovimentacaoDTO(0, carteira.getIdentificador(), LocalDate.of(2026, 1, 1),
                TipoMovimentacao.CREDITO, 100);
        assertThrows(PersistenciaException.class, () -> persistencia.movimentacoes().incluir(credito));

        conexoes.criarTabelas();

        assertDoesNotThrow(() -> persistencia.movimentacoes().incluir(credito));
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
}
