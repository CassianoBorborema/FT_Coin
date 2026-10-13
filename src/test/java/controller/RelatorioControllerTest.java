package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.memoria.CarteiraDAOMemoria;
import dao.memoria.MovimentacaoDAOMemoria;
import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.CotacoesFixas;

class RelatorioControllerTest {

    private static final double TOLERANCIA = 1e-9;
    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);
    private static final LocalDate ONTEM = HOJE.minusDays(1);

    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movimentacaoDAO;
    private CotacoesFixas cotacoes;
    private RelatorioController controller;

    @BeforeEach
    void setUp() {
        carteiraDAO = new CarteiraDAOMemoria();
        movimentacaoDAO = new MovimentacaoDAOMemoria();
        cotacoes = new CotacoesFixas();
        controller = new RelatorioController(carteiraDAO, movimentacaoDAO, cotacoes,
                CotacoesFixas.relogioEm(HOJE));
    }

    @Test
    void listarPorTitularIgnoraMaiusculas() throws AppException {
        carteiraDAO.incluir(new CarteiraDTO(0, "carla", "XP"));
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        carteiraDAO.incluir(new CarteiraDTO(0, "Bruno", "XP"));

        assertEquals(List.of("Ana", "Bruno", "carla"), titulares(controller.listarPorTitular()));
    }

    @Test
    void listarPorIdentificadorOrdenaCrescente() throws AppException {
        carteiraDAO.incluir(new CarteiraDTO(3, "C", "XP"));
        carteiraDAO.incluir(new CarteiraDTO(1, "A", "XP"));
        carteiraDAO.incluir(new CarteiraDTO(2, "B", "XP"));

        assertEquals(List.of(1, 2, 3), controller.listarPorIdentificador().stream()
                .map(CarteiraDTO::getIdentificador).collect(Collectors.toList()));
    }

    @Test
    void apurarValoraSaldoPelaCotacaoDeHoje() throws AppException {
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        cotacoes.com(ONTEM, 10.0).com(HOJE, 12.0);
        movimentacaoDAO.incluir(new MovimentacaoDTO(0, 1, ONTEM, TipoMovimentacao.COMPRA, 100));

        ApuracaoDTO apuracao = controller.apurar(1);

        assertEquals(12.0, apuracao.getCotacaoAtual(), TOLERANCIA);
        assertEquals(1_200, apuracao.getSaldoMercado(), TOLERANCIA);
        assertEquals(200, apuracao.getResultadoMercado(), TOLERANCIA);
        assertEquals(0, apuracao.getResultadoCustoMedio(), TOLERANCIA);
    }

    @Test
    void apurarSemCotacaoDeHojeFalha() throws AppException {
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        cotacoes.com(ONTEM, 10.0);
        movimentacaoDAO.incluir(new MovimentacaoDTO(0, 1, ONTEM, TipoMovimentacao.COMPRA, 100));

        assertThrows(AppException.class, () -> controller.apurar(1));
    }

    @Test
    void apurarCarteiraInexistenteFalha() {
        assertThrows(AppException.class, () -> controller.apurar(1));
        assertThrows(AppException.class, () -> controller.apurar(0));
    }

    private static List<String> titulares(List<CarteiraDTO> carteiras) {
        return carteiras.stream().map(CarteiraDTO::getNomeTitular).collect(Collectors.toList());
    }
}
