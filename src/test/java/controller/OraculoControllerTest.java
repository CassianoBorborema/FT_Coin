package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.CotacoesFixas;

class OraculoControllerTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);

    private OraculoController controller;

    @BeforeEach
    void setUp() {
        CotacoesFixas cotacoes = new CotacoesFixas()
                .com(HOJE.minusDays(2), 10.0)
                .com(HOJE.minusDays(1), 11.0)
                .com(HOJE, 12.0);
        controller = new OraculoController(cotacoes, CotacoesFixas.relogioEm(HOJE));
    }

    @Test
    void cotacaoDeHojeUsaDataDoSistema() throws AppException {
        OraculoDTO cotacao = controller.cotacaoDeHoje();

        assertEquals(HOJE, cotacao.getData());
        assertEquals(12.0, cotacao.getCotacao());
    }

    @Test
    void consultarExigeData() {
        assertThrows(AppException.class, () -> controller.consultar(null));
    }

    @Test
    void listarPeriodoIncluiPrimeiroEUltimoDia() throws AppException {
        List<OraculoDTO> cotacoes = controller.listarPeriodo(HOJE.minusDays(2), HOJE);

        assertEquals(List.of(10.0, 11.0, 12.0),
                cotacoes.stream().map(OraculoDTO::getCotacao).collect(Collectors.toList()));
    }

    @Test
    void listarPeriodoInvertidoFalha() {
        assertThrows(AppException.class, () -> controller.listarPeriodo(HOJE, HOJE.minusDays(1)));
    }

    @Test
    void listarPeriodoMuitoLongoFalha() {
        LocalDate inicio = HOJE.minusDays(OraculoController.MAXIMO_DIAS_PERIODO);
        assertThrows(AppException.class, () -> controller.listarPeriodo(inicio, HOJE));
    }
}
