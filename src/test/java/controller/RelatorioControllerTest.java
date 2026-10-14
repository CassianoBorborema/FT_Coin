package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.Persistencia;
import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;
import model.ProvedorCotacao;
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
        carteiraDAO = Persistencia.emMemoria().carteiras();
        movimentacaoDAO = Persistencia.emMemoria().movimentacoes();
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

        assertEquals(12.0, apuracao.cotacaoAtual(), TOLERANCIA);
        assertEquals(1_200, apuracao.saldoMercado(), TOLERANCIA);
        assertEquals(200, apuracao.resultadoMercado(), TOLERANCIA);
        assertEquals(0, apuracao.resultadoCustoMedio(), TOLERANCIA);
    }

    @Test
    void cotacaoAtualEhConsultadaNaDataDoSistema() throws AppException {
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        ProvedorCotacao oraculo = spy(new CotacoesFixas().com(HOJE, 10.0));
        RelatorioController relatorios = new RelatorioController(carteiraDAO, movimentacaoDAO, oraculo,
                CotacoesFixas.relogioEm(HOJE));

        relatorios.apurar(1);

        verify(oraculo).cotacaoEm(HOJE);
    }

    @Test
    void semCotacaoDeHojeUsaAUltimaDisponivel() throws AppException {
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        cotacoes.com(ONTEM, 10.0);
        movimentacaoDAO.incluir(new MovimentacaoDTO(0, 1, ONTEM, TipoMovimentacao.COMPRA, 100));

        ApuracaoDTO apuracao = controller.apurar(1);

        assertEquals(10.0, apuracao.cotacaoAtual(), TOLERANCIA);
        assertEquals(ONTEM, apuracao.dataCotacaoAtual());
    }

    @Test
    void evolucaoDiariaVaiDaPrimeiraMovimentacaoAteHoje() throws AppException {
        carteiraDAO.incluir(new CarteiraDTO(0, "Ana", "XP"));
        cotacoes.com(ONTEM.minusDays(1), 9.0).com(ONTEM, 10.0).com(HOJE, 12.0);
        movimentacaoDAO.incluir(new MovimentacaoDTO(0, 1, ONTEM, TipoMovimentacao.COMPRA, 100));

        List<ApuracaoDTO.Dia> dias = controller.evolucaoDiaria(1);

        assertEquals(List.of(ONTEM, HOJE), dias.stream().map(ApuracaoDTO.Dia::data).toList());
        assertEquals(1_200, dias.getLast().saldoMercado(), TOLERANCIA);
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
