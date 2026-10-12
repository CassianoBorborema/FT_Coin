package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.memoria.CarteiraDAOMemoria;
import dao.memoria.MovimentacaoDAOMemoria;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.time.LocalDate;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.CotacoesFixas;

class MovimentacaoControllerTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);

    private MovimentacaoDAO movimentacaoDAO;
    private MovimentacaoController controller;

    @BeforeEach
    void setUp() throws AppException {
        CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
        movimentacaoDAO = new MovimentacaoDAOMemoria();
        controller = new MovimentacaoController(movimentacaoDAO, carteiraDAO,
                new CotacoesFixas().com(HOJE, 10.0), CotacoesFixas.relogioEm(HOJE));

        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
    }

    @Test
    void compraEhRegistradaNaDataDoSistema() throws AppException {
        MovimentacaoDTO compra = controller.registrarCompra(1, 10);

        assertEquals(TipoMovimentacao.COMPRA, compra.getTipo());
        assertEquals(HOJE, compra.getData());
        assertEquals(10, movimentacaoDAO.calcularSaldo(1));
    }

    @Test
    void vendaReduzSaldo() throws AppException {
        controller.registrarCompra(1, 10);

        MovimentacaoDTO venda = controller.registrarVenda(1, 4);

        assertEquals(HOJE, venda.getData());
        assertEquals(6, movimentacaoDAO.calcularSaldo(1));
    }

    @Test
    void vendaAcimaDoSaldoEhRecusada() throws AppException {
        controller.registrarCompra(1, 10);

        assertThrows(AppException.class, () -> controller.registrarVenda(1, 11));
    }

    @Test
    void movimentacaoEmCarteiraInexistenteEhRecusada() {
        assertThrows(AppException.class, () -> controller.registrarCompra(99, 10));
        assertThrows(AppException.class, () -> controller.validarCarteiraExiste(0));
    }

    @Test
    void movimentacaoSemCotacaoDeHojeEhRecusada() throws AppException {
        CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        MovimentacaoController semCotacao = new MovimentacaoController(new MovimentacaoDAOMemoria(),
                carteiraDAO, new CotacoesFixas(), CotacoesFixas.relogioEm(HOJE));

        assertThrows(AppException.class, () -> semCotacao.registrarCompra(1, 10));
    }

    @Test
    void cotacaoDeHojeVemDoOraculo() throws AppException {
        assertEquals(10.0, controller.cotacaoDeHoje());
    }

    @Test
    void quantidadeDeveSerPositiva() {
        assertThrows(AppException.class, () -> controller.registrarCompra(1, 0));
        assertThrows(AppException.class, () -> controller.registrarCompra(1, -1));
    }
}
