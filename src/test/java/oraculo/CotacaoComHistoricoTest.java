package oraculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dao.OraculoDAO;
import dao.Persistencia;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.CotacoesFixas;
import testutil.RegistroDeLogs;

class CotacaoComHistoricoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 1, 10);

    private CotacoesFixas fonte;
    private OraculoDAO historico;
    private CotacaoComHistorico cotacoes;

    @BeforeEach
    void setUp() {
        fonte = new CotacoesFixas().com(DIA, 13.84);
        historico = Persistencia.emMemoria().cotacoes();
        cotacoes = new CotacaoComHistorico(fonte, historico);
    }

    @Test
    void cotacaoObtidaDaFonteFicaRegistrada() throws AppException {
        assertEquals(13.84, cotacoes.cotacaoEm(DIA));

        assertTrue(historico.existe(DIA));
        assertEquals(13.84, historico.consultarPorData(DIA).getCotacao());
    }

    @Test
    void gravacaoNoHistoricoEhRegistradaNoLog() throws AppException {
        try (RegistroDeLogs logs = new RegistroDeLogs(CotacaoComHistorico.class)) {
            cotacoes.cotacaoEm(DIA);
            cotacoes.cotacaoEm(DIA);

            assertEquals(1, logs.eventos().size());
            assertTrue(logs.texto().contains("INFO Cotação de 2026-01-10 obtida da fonte e gravada no histórico: 13.84"));
        }
    }

    @Test
    void cotacaoRegistradaNaoConsultaFonteDeNovo() throws AppException {
        cotacoes.cotacaoEm(DIA);
        cotacoes.cotacaoEm(DIA);

        assertEquals(1, fonte.getConsultas());
    }

    @Test
    void historicoPrevaleceSobreFonte() throws AppException {
        historico.incluir(new OraculoDTO(DIA, 99.0));

        assertEquals(99.0, cotacoes.cotacaoEm(DIA));
        assertEquals(0, fonte.getConsultas());
    }

    @Test
    void erroDaFonteNaoRegistraNada() {
        LocalDate semCotacao = DIA.plusDays(1);

        assertThrows(AppException.class, () -> cotacoes.cotacaoEm(semCotacao));
        assertFalse(existeNoHistorico(semCotacao));
    }

    private boolean existeNoHistorico(LocalDate data) {
        try {
            return historico.existe(data);
        } catch (AppException e) {
            throw new AssertionError(e);
        }
    }
}
