package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.OraculoDAO;
import dao.memoria.CarteiraDAOMemoria;
import dao.memoria.MovimentacaoDAOMemoria;
import dao.memoria.OraculoDAOMemoria;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MovimentacaoControllerTest {

    private static final LocalDate HOJE = LocalDate.now();

    private MovimentacaoDAO movimentacaoDAO;
    private MovimentacaoController controller;

    @BeforeEach
    void setUp() throws AppException {
        CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
        OraculoDAO oraculoDAO = new OraculoDAOMemoria();
        movimentacaoDAO = new MovimentacaoDAOMemoria();
        controller = new MovimentacaoController(movimentacaoDAO, carteiraDAO, oraculoDAO);

        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        oraculoDAO.incluir(new OraculoDTO(HOJE, 10.0));
    }

    @Test
    void compraRegistraMovimentacao() throws AppException {
        MovimentacaoDTO compra = controller.registrarCompra(1, HOJE, 10);

        assertEquals(TipoMovimentacao.COMPRA, compra.getTipo());
        assertEquals(10, movimentacaoDAO.calcularSaldo(1));
    }

    @Test
    void vendaReduzSaldo() throws AppException {
        controller.registrarCompra(1, HOJE, 10);

        controller.registrarVenda(1, HOJE, 4);

        assertEquals(6, movimentacaoDAO.calcularSaldo(1));
    }

    @Test
    void vendaAcimaDoSaldoEhRecusada() throws AppException {
        controller.registrarCompra(1, HOJE, 10);

        assertThrows(AppException.class, () -> controller.registrarVenda(1, HOJE, 11));
    }

    @Test
    void movimentacaoEmCarteiraInexistenteEhRecusada() {
        assertThrows(AppException.class, () -> controller.registrarCompra(99, HOJE, 10));
    }

    @Test
    void movimentacaoSemCotacaoNaDataEhRecusada() {
        assertThrows(AppException.class, () -> controller.registrarCompra(1, HOJE.minusDays(10), 10));
    }

    @Test
    void quantidadeDeveSerPositiva() {
        assertThrows(AppException.class, () -> controller.registrarCompra(1, HOJE, 0));
        assertThrows(AppException.class, () -> controller.registrarCompra(1, HOJE, -1));
    }
}
