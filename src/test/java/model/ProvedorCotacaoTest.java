package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import dto.OraculoDTO;
import exception.AppException;
import exception.PersistenciaException;
import java.sql.SQLException;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import testutil.CotacoesFixas;

class ProvedorCotacaoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);

    @Test
    void ultimaCotacaoEhADeHojeQuandoExiste() throws AppException {
        OraculoDTO cotacao = new CotacoesFixas().com(HOJE, 12.0).com(HOJE.minusDays(1), 11.0)
                .ultimaCotacaoAte(HOJE);

        assertEquals(HOJE, cotacao.getData());
        assertEquals(12.0, cotacao.getCotacao());
    }

    @Test
    void semCotacaoHojeVoltaAteAUltimaDisponivel() throws AppException {
        OraculoDTO cotacao = new CotacoesFixas().com(HOJE.minusDays(3), 9.5).ultimaCotacaoAte(HOJE);

        assertEquals(HOJE.minusDays(3), cotacao.getData());
        assertEquals(9.5, cotacao.getCotacao());
    }

    @Test
    void semCotacaoNoPeriodoDeBuscaFalha() {
        CotacoesFixas antiga = new CotacoesFixas().com(HOJE.minusDays(ProvedorCotacao.DIAS_BUSCA_ULTIMA_COTACAO), 9.0);

        assertThrows(AppException.class, () -> antiga.ultimaCotacaoAte(HOJE));
    }

    @Test
    void falhaDoBancoInterrompeABusca() throws AppException {
        ProvedorCotacao bancoFora = mock(ProvedorCotacao.class, withSettings().defaultAnswer(Answers.CALLS_REAL_METHODS));
        when(bancoFora.cotacaoEm(any())).thenThrow(
                new PersistenciaException(new SQLException("conexão recusada"), "banco fora do ar"));

        assertThrows(PersistenciaException.class, () -> bancoFora.ultimaCotacaoAte(HOJE));
    }
}
