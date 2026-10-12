package oraculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dao.OraculoDAO;
import dao.memoria.OraculoDAOMemoria;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import testutil.CotacoesFixas;

class CotacaoComHistoricoTest {

    private static final LocalDate DIA = LocalDate.of(2026, 1, 10);

    private CotacoesFixas fonte;
    private OraculoDAO historico;
    private CotacaoComHistorico cotacoes;

    @BeforeEach
    void setUp() {
        fonte = new CotacoesFixas().com(DIA, 13.84);
        historico = new OraculoDAOMemoria();
        cotacoes = new CotacaoComHistorico(fonte, historico);
    }

    @Test
    void cotacaoObtidaDaFonteFicaRegistrada() throws AppException {
        assertEquals(13.84, cotacoes.cotacaoEm(DIA));

        assertTrue(historico.existe(DIA));
        assertEquals(13.84, historico.consultarPorData(DIA).getCotacao());
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
