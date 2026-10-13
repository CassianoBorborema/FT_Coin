package oraculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.AppException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import testutil.CotacoesFixas;

class OraculoSimuladoTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);
    private static final double AMPLITUDE = 2.5;

    private final OraculoSimulado oraculo = criar(AMPLITUDE);

    @ParameterizedTest(name = "dia {0}: valorização {1}")
    @CsvSource({"1, 1.40", "2, 1.70", "5, 2.10", "10, 2.40", "20, 2.70", "30, 2.88"})
    void valorizacaoReproduzPlanilha(int dia, double esperada) {
        assertEquals(esperada, oraculo.valorizacao(INICIO.plusDays(dia - 1)), 0.005);
    }

    @Test
    void semFlutuacaoCotacaoEhBaseMaisValorizacao() throws AppException {
        OraculoSimulado semFlutuacao = criar(0);

        assertEquals(11.40, semFlutuacao.cotacaoEm(INICIO));
        assertEquals(12.88, semFlutuacao.cotacaoEm(HOJE));
    }

    @Test
    void mesmaDataTemSempreMesmaCotacao() throws AppException {
        assertEquals(oraculo.cotacaoEm(HOJE), criar(AMPLITUDE).cotacaoEm(HOJE));
    }

    @Test
    void flutuacaoFicaDentroDaAmplitudeEVariaEntreOsDias() {
        double primeira = oraculo.flutuacao(INICIO);
        boolean variou = false;
        for (LocalDate data = INICIO; !data.isAfter(HOJE); data = data.plusDays(1)) {
            double flutuacao = oraculo.flutuacao(data);
            assertTrue(Math.abs(flutuacao) <= AMPLITUDE, "flutuação fora da amplitude em " + data);
            variou |= flutuacao != primeira;
        }
        assertTrue(variou);
    }

    @Test
    void cotacaoTemDuasCasasDecimais() throws AppException {
        for (LocalDate data = INICIO; !data.isAfter(HOJE); data = data.plusDays(1)) {
            assertTrue(BigDecimal.valueOf(oraculo.cotacaoEm(data)).scale() <= 2, "mais de 2 casas em " + data);
        }
    }

    @Test
    void sementeDiferenteGeraOutraSerie() throws AppException {
        OraculoSimulado outraSemente = new OraculoSimulado(
                new ParametrosOraculoSimulado(INICIO, 10, AMPLITUDE, 1.40, 0.435, 7), CotacoesFixas.relogioEm(HOJE));

        assertNotEquals(oraculo.cotacaoEm(HOJE), outraSemente.cotacaoEm(HOJE));
    }

    @Test
    void naoHaCotacaoAntesDaDataInicial() {
        assertThrows(AppException.class, () -> oraculo.cotacaoEm(INICIO.minusDays(1)));
    }

    @Test
    void naoHaCotacaoFutura() {
        assertThrows(AppException.class, () -> oraculo.cotacaoEm(HOJE.plusDays(1)));
    }

    @Test
    void parametrosQuePermitemCotacaoNaoPositivaSaoRecusados() {
        assertThrows(IllegalArgumentException.class,
                () -> new ParametrosOraculoSimulado(INICIO, 1, 3, 1.40, 0.435, 1));
    }

    private static OraculoSimulado criar(double amplitude) {
        return new OraculoSimulado(new ParametrosOraculoSimulado(INICIO, 10, amplitude, 1.40, 0.435, 2026),
                CotacoesFixas.relogioEm(HOJE));
    }
}
