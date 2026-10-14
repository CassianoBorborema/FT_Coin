package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.Persistencia;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import exception.PersistenciaException;
import java.sql.SQLException;
import java.time.LocalDate;
import model.ProvedorCotacao;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.CotacoesFixas;

class MovimentacaoControllerTest {

    private static final double TOLERANCIA = 1e-9;
    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);
    private static final LocalDate ONTEM = HOJE.minusDays(1);

    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movimentacaoDAO;
    private MovimentacaoController controller;

    @BeforeEach
    void setUp() throws AppException {
        Persistencia persistencia = Persistencia.emMemoria();
        carteiraDAO = persistencia.carteiras();
        movimentacaoDAO = persistencia.movimentacoes();
        controller = criarController(new CotacoesFixas().com(ONTEM, 8.0).com(HOJE, 10.0), HOJE);
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
    }

    @Test
    void compraEhRegistradaNaDataDoSistemaEDebitaOSaldoEmReais() throws AppException {
        controller.creditar(1, 150);

        MovimentacaoDTO compra = controller.registrarCompra(1, 10);

        assertEquals(TipoMovimentacao.COMPRA, compra.getTipo());
        assertEquals(HOJE, compra.getData());
        assertEquals(50, controller.saldoEmReais(1), TOLERANCIA);
    }

    @Test
    void compraSemSaldoEmReaisEhRecusada() throws AppException {
        controller.creditar(1, 99);

        AppException erro = assertThrows(AppException.class, () -> controller.registrarCompra(1, 10));
        assertTrue(erro.getMessage().contains("Saldo em reais insuficiente"));
        assertFalse(temMovimentacaoDoTipo(TipoMovimentacao.COMPRA));
    }

    @Test
    void compraAntesDaVigenciaNaoExigeNemDebitaSaldoEmReais() throws AppException {
        MovimentacaoController antesDaVigencia = criarController(new CotacoesFixas().com(HOJE, 10.0),
                HOJE.plusDays(1));

        antesDaVigencia.registrarCompra(1, 10);

        assertEquals(0, antesDaVigencia.saldoEmReais(1), TOLERANCIA);
    }

    @Test
    void vendaCreditaOSaldoEmReais() throws AppException {
        controller.creditar(1, 100);
        controller.registrarCompra(1, 10);

        controller.registrarVenda(1, 4);

        assertEquals(40, controller.saldoEmReais(1), TOLERANCIA);
    }

    @Test
    void vendaAcimaDoSaldoDeFTCoinsEhRecusada() throws AppException {
        controller.creditar(1, 100);
        controller.registrarCompra(1, 10);

        AppException erro = assertThrows(AppException.class, () -> controller.registrarVenda(1, 11));
        assertTrue(erro.getMessage().contains("excede o saldo de 10 FTCoins"));
    }

    @Test
    void saqueLimitadoAoSaldoEmReais() throws AppException {
        controller.creditar(1, 50);

        controller.sacar(1, 20);

        assertEquals(30, controller.saldoEmReais(1), TOLERANCIA);
        assertThrows(AppException.class, () -> controller.sacar(1, 30.01));
    }

    @Test
    void movimentacaoEmCarteiraInexistenteEhRecusada() {
        assertThrows(AppException.class, () -> controller.creditar(99, 10));
        assertThrows(AppException.class, () -> controller.validarCarteiraExiste(0));
    }

    @Test
    void movimentacaoSemCotacaoDeHojeEhRecusada() {
        MovimentacaoController semCotacao = criarController(new CotacoesFixas(), HOJE);

        assertThrows(AppException.class, () -> semCotacao.creditar(1, 10));
    }

    @Test
    void falhaAoObterCotacaoNaoRegistraMovimentacao() throws AppException {
        ProvedorCotacao cotacoes = mock(ProvedorCotacao.class);
        when(cotacoes.cotacaoEm(any())).thenThrow(
                new PersistenciaException("banco fora do ar", new SQLException("conexão recusada")));
        MovimentacaoController comFalha = criarController(cotacoes, HOJE);

        assertThrows(PersistenciaException.class, () -> comFalha.creditar(1, 10));
        assertFalse(movimentacaoDAO.possuiMovimentacoes(1));
    }

    @Test
    void cotacaoDeHojeVemDoOraculo() throws AppException {
        assertEquals(10.0, controller.cotacaoDeHoje());
    }

    @Test
    void quantidadeDeveSerPositiva() {
        assertThrows(AppException.class, () -> controller.creditar(1, 0));
        assertThrows(AppException.class, () -> controller.registrarCompra(1, -1));
    }

    private MovimentacaoController criarController(ProvedorCotacao cotacoes, LocalDate vigencia) {
        return new MovimentacaoController(movimentacaoDAO, carteiraDAO, cotacoes, CotacoesFixas.relogioEm(HOJE),
                vigencia);
    }

    private boolean temMovimentacaoDoTipo(TipoMovimentacao tipo) throws AppException {
        return movimentacaoDAO.listarPorCarteira(1).stream().anyMatch(m -> m.getTipo() == tipo);
    }
}
