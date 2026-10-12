package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.OraculoDAO;
import dao.memoria.CarteiraDAOMemoria;
import dao.memoria.MovimentacaoDAOMemoria;
import dao.memoria.OraculoDAOMemoria;
import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RelatorioControllerTest {

    private static final double TOLERANCIA = 1e-9;
    private static final LocalDate HOJE = LocalDate.now();
    private static final LocalDate ONTEM = HOJE.minusDays(1);

    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movimentacaoDAO;
    private OraculoDAO oraculoDAO;
    private RelatorioController controller;

    @BeforeEach
    void setUp() {
        carteiraDAO = new CarteiraDAOMemoria();
        movimentacaoDAO = new MovimentacaoDAOMemoria();
        oraculoDAO = new OraculoDAOMemoria();
        controller = new RelatorioController(carteiraDAO, movimentacaoDAO, oraculoDAO);
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
        oraculoDAO.incluir(new OraculoDTO(ONTEM, 10.0));
        oraculoDAO.incluir(new OraculoDTO(HOJE, 12.0));
        movimentacaoDAO.incluir(new MovimentacaoDTO(0, 1, ONTEM, TipoMovimentacao.COMPRA, 100));

        ApuracaoDTO apuracao = controller.apurar(1);

        assertEquals(12.0, apuracao.getCotacaoAtual(), TOLERANCIA);
        assertEquals(1_200, apuracao.getSaldoMercado(), TOLERANCIA);
        assertEquals(200, apuracao.getResultadoMercado(), TOLERANCIA);
        assertEquals(0, apuracao.getResultadoCustoMedio(), TOLERANCIA);
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
