package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dto.ApuracaoDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApuracaoCarteiraTest {

    private static final double TOLERANCIA = 1e-9;
    private static final LocalDate DIA_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DIA_2 = DIA_1.plusDays(1);
    private static final LocalDate DIA_3 = DIA_1.plusDays(2);

    /** Cotação igual ao número do dia do mês: 1, 2, 3... */
    private static final ProvedorCotacao COTACAO_DO_DIA = data -> data.getDayOfMonth();

    @Test
    void carteiraSemMovimentacoesTemTudoZerado() throws AppException {
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(List.of(), COTACAO_DO_DIA);

        assertTrue(apuracao.getLinhas().isEmpty());
        assertEquals(0, apuracao.getSaldoQuantidade(), TOLERANCIA);
        assertEquals(0, apuracao.getCustoMedio(), TOLERANCIA);
        assertEquals(0, apuracao.getResultadoCustoMedio(), TOLERANCIA);
        assertEquals(0, apuracao.toDTO(1, 10).percentualCustoMedio(), TOLERANCIA);
    }

    @Test
    void compraRecalculaCustoMedioPonderado() throws AppException {
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(List.of(
                compra(1, DIA_1, 10),   // 10 x 1 = 10
                compra(2, DIA_3, 10)    // 10 x 3 = 30
        ), COTACAO_DO_DIA);

        assertEquals(20, apuracao.getSaldoQuantidade(), TOLERANCIA);
        assertEquals(2, apuracao.getCustoMedio(), TOLERANCIA);
        assertEquals(40, apuracao.getTotalComprado(), TOLERANCIA);
    }

    @Test
    void vendaMantemCustoMedioEApuraResultado() throws AppException {
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(List.of(
                compra(1, DIA_1, 10),
                venda(2, DIA_3, 4)      // 4 x (3 - 1) = 8
        ), COTACAO_DO_DIA);

        ApuracaoDTO.Linha venda = apuracao.getLinhas().get(1);
        assertEquals(8, venda.resultadoOperacao(), TOLERANCIA);
        assertEquals(1, apuracao.getCustoMedio(), TOLERANCIA);
        assertEquals(6, apuracao.getSaldoQuantidade(), TOLERANCIA);
        assertEquals(12 - 10, apuracao.getResultadoOperacional(), TOLERANCIA);
    }

    @Test
    void venderTodoSaldoZeraCustoMedio() throws AppException {
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(List.of(
                compra(1, DIA_1, 10),
                venda(2, DIA_2, 10),
                compra(3, DIA_3, 5)
        ), COTACAO_DO_DIA);

        assertEquals(0, apuracao.getLinhas().get(1).saldoQuantidade(), TOLERANCIA);
        assertEquals(3, apuracao.getCustoMedio(), TOLERANCIA);
    }

    @Test
    void movimentacoesSaoApuradasEmOrdemCronologica() throws AppException {
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(List.of(
                venda(2, DIA_3, 5),
                compra(1, DIA_1, 10)
        ), COTACAO_DO_DIA);

        assertEquals(DIA_1, apuracao.getLinhas().get(0).data());
        assertEquals(5, apuracao.getSaldoQuantidade(), TOLERANCIA);
    }

    @Test
    void vendaAcimaDoSaldoNaDataEhInconsistente() {
        assertThrows(AppException.class, () -> ApuracaoCarteira.calcular(List.of(
                venda(1, DIA_1, 5),
                compra(2, DIA_2, 10)
        ), COTACAO_DO_DIA));
    }

    @Test
    void cotacaoAusentePropagaErro() {
        ProvedorCotacao semCotacao = data -> {
            throw new AppException("sem cotação");
        };
        assertThrows(AppException.class,
                () -> ApuracaoCarteira.calcular(List.of(compra(1, DIA_1, 10)), semCotacao));
    }

    @Test
    void resultadosAMercadoUsamCotacaoInformada() throws AppException {
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(List.of(
                compra(1, DIA_2, 10)    // custo 20
        ), COTACAO_DO_DIA);

        assertEquals(-20 + 10 * 5, apuracao.getResultadoMercado(5), TOLERANCIA);
        assertEquals(150, apuracao.toDTO(1, 5).percentualMercado(), TOLERANCIA);
    }

    private static MovimentacaoDTO compra(int id, LocalDate data, double quantidade) {
        return new MovimentacaoDTO(id, 1, data, TipoMovimentacao.COMPRA, quantidade);
    }

    private static MovimentacaoDTO venda(int id, LocalDate data, double quantidade) {
        return new MovimentacaoDTO(id, 1, data, TipoMovimentacao.VENDA, quantidade);
    }
}
