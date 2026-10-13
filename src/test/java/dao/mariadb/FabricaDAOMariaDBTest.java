package dao.mariadb;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dao.FabricaDAO;
import dao.FabricaDAOContrato;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import exception.PersistenciaException;
import infra.db.ConfiguracaoBanco;
import infra.db.FabricaConexao;
import infra.db.InicializadorBanco;
import java.sql.Statement;
import java.time.LocalDate;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mariadb.MariaDBContainer;

/**
 * Roda o contrato dos DAOs contra um MariaDB real em contêiner. Sem Docker disponível,
 * os testes desta classe são ignorados.
 */
@Testcontainers(disabledWithoutDocker = true)
class FabricaDAOMariaDBTest extends FabricaDAOContrato {

    @Container
    private static final MariaDBContainer MARIADB = new MariaDBContainer("mariadb:11.4");

    private static FabricaConexao conexoes;

    @BeforeAll
    static void criarTabelas() throws AppException {
        conexoes = new FabricaConexao(new ConfiguracaoBanco(
                MARIADB.getJdbcUrl(), MARIADB.getUsername(), MARIADB.getPassword(), 5));
        new InicializadorBanco(conexoes).inicializar();
    }

    @Override
    protected FabricaDAO criarFabricaVazia() throws AppException {
        conexoes.executar("limpar as tabelas", conexao -> {
            try (Statement statement = conexao.createStatement()) {
                statement.execute("DELETE FROM movimentacao");
                statement.execute("DELETE FROM carteira");
                statement.execute("DELETE FROM cotacao");
            }
            return null;
        });
        return new FabricaDAOMariaDB(conexoes);
    }

    @Test
    void inicializarDeNovoNaoApagaNemDuplicaTabelas() throws AppException {
        FabricaDAO fabrica = criarFabricaVazia();
        CarteiraDTO carteira = new CarteiraDTO(0, "Ana", "XP");
        fabrica.carteiraDAO().incluir(carteira);

        assertDoesNotThrow(() -> new InicializadorBanco(conexoes).inicializar());
        assertDoesNotThrow(() -> fabrica.carteiraDAO().consultarPorId(carteira.getIdentificador()));
    }

    @Test
    void bancoRecusaMovimentacaoDeCarteiraInexistente() throws AppException {
        FabricaDAO fabrica = criarFabricaVazia();

        assertThrows(PersistenciaException.class, () -> fabrica.movimentacaoDAO().incluir(
                new MovimentacaoDTO(0, 999, LocalDate.of(2026, 1, 1), TipoMovimentacao.COMPRA, 1)));
    }

    @Test
    void bancoRecusaExcluirCarteiraComMovimentacoes() throws AppException {
        FabricaDAO fabrica = criarFabricaVazia();
        CarteiraDTO carteira = new CarteiraDTO(0, "Ana", "XP");
        fabrica.carteiraDAO().incluir(carteira);
        fabrica.movimentacaoDAO().incluir(new MovimentacaoDTO(0, carteira.getIdentificador(),
                LocalDate.of(2026, 1, 1), TipoMovimentacao.COMPRA, 1));

        assertThrows(PersistenciaException.class,
                () -> fabrica.carteiraDAO().excluir(carteira.getIdentificador()));
    }
}
