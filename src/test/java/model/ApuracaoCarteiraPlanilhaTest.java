package model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import dto.ApuracaoDTO;
import dto.MovimentacaoDTO;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reproduz a planilha de referência "FTCoinApuracaoResultado" (simulação de janeiro/2026).
 * <p>
 * A planilha só exibe cotações com duas casas; a cotação de cada operação é obtida dividindo o
 * valor financeiro exibido pela quantidade (ex.: 12.287,94 / 1.000 = 12,28794). Como esses valores
 * já vêm arredondados ao centavo, os acumulados podem divergir da planilha em alguns centavos,
 * absorvidos pela {@link #TOLERANCIA_ACUMULADA}.
 * </p>
 */
class ApuracaoCarteiraPlanilhaTest {

    private static final double TOLERANCIA_CENTAVO = 0.005;
    private static final double TOLERANCIA_ACUMULADA = 0.03;
    private static final double TOLERANCIA_PERCENTUAL = 0.05;

    private static final double COMPRA = 1_000;
    private static final double VENDA = 500;

    private static final double COTACAO_FINAL = 14_655.06 / 1_000;

    /**
     * Linhas da planilha que têm operação: dia, tipo, valor financeiro, saldo (qtd), custo médio,
     * saldo financeiro, resultado da operação e resultado acumulado.
     */
    private static final Object[][] PLANILHA = {
            {1, TipoMovimentacao.COMPRA, 12_287.94, 1_000, 12.29, 12_287.94, 0.00, 0.00},
            {5, TipoMovimentacao.VENDA, 5_278.46, 500, 12.29, 6_143.97, -865.51, -865.51},
            {6, TipoMovimentacao.COMPRA, 13_606.09, 1_500, 13.17, 19_750.06, 0.00, -865.51},
            {9, TipoMovimentacao.VENDA, 6_216.09, 1_000, 13.17, 13_166.71, -367.26, -1_232.78},
            {10, TipoMovimentacao.VENDA, 6_918.97, 500, 13.17, 6_583.35, 335.62, -897.16},
            {11, TipoMovimentacao.COMPRA, 14_209.33, 1_500, 13.86, 20_792.69, 0.00, -897.16},
            {13, TipoMovimentacao.VENDA, 5_735.94, 1_000, 13.86, 13_861.79, -1_194.95, -2_092.12},
            {15, TipoMovimentacao.VENDA, 6_282.02, 500, 13.86, 6_930.90, -648.88, -2_740.99},
            {16, TipoMovimentacao.COMPRA, 12_602.06, 1_500, 13.02, 19_532.96, 0.00, -2_740.99},
            {18, TipoMovimentacao.VENDA, 7_306.61, 1_000, 13.02, 13_021.97, 795.62, -1_945.37},
            {20, TipoMovimentacao.VENDA, 5_779.49, 500, 13.02, 6_510.99, -731.50, -2_676.87},
            {21, TipoMovimentacao.COMPRA, 12_060.16, 1_500, 12.38, 18_571.14, 0.00, -2_676.87},
            {23, TipoMovimentacao.VENDA, 5_459.83, 1_000, 12.38, 12_380.76, -730.55, -3_407.42},
            {25, TipoMovimentacao.VENDA, 6_512.94, 500, 12.38, 6_190.38, 322.56, -3_084.86},
            {26, TipoMovimentacao.COMPRA, 12_032.91, 1_500, 12.15, 18_223.29, 0.00, -3_084.86},
            {28, TipoMovimentacao.VENDA, 6_017.55, 1_000, 12.15, 12_148.86, -56.88, -3_141.75},
    };

    /** Todos os dias da planilha: dia, saldo (qtd), saldo financeiro, resultado acumulado e saldo a mercado. */
    private static final double[][] DIAS = {
            {1, 1_000, 12_287.94, 0.00, 12_287.94},
            {2, 1_000, 12_287.94, 0.00, 13_868.97},
            {3, 1_000, 12_287.94, 0.00, 12_165.06},
            {4, 1_000, 12_287.94, 0.00, 12_100.00},
            {5, 500, 6_143.97, -865.51, 5_278.46},
            {6, 1_500, 19_750.06, -865.51, 20_409.14},
            {7, 1_500, 19_750.06, -865.51, 21_019.56},
            {8, 1_500, 19_750.06, -865.51, 16_681.54},
            {9, 1_000, 13_166.71, -1_232.78, 12_432.18},
            {10, 500, 6_583.35, -897.16, 6_918.97},
            {11, 1_500, 20_792.69, -897.16, 21_314.00},
            {12, 1_500, 20_792.69, -897.16, 20_155.68},
            {13, 1_000, 13_861.79, -2_092.12, 11_471.88},
            {14, 1_000, 13_861.79, -2_092.12, 14_224.07},
            {15, 500, 6_930.90, -2_740.99, 6_282.02},
            {16, 1_500, 19_532.96, -2_740.99, 18_903.09},
            {17, 1_500, 19_532.96, -2_740.99, 19_437.58},
            {18, 1_000, 13_021.97, -1_945.37, 14_613.21},
            {19, 1_000, 13_021.97, -1_945.37, 14_606.69},
            {20, 500, 6_510.99, -2_676.87, 5_779.49},
            {21, 1_500, 18_571.14, -2_676.87, 18_090.24},
            {22, 1_500, 18_571.14, -2_676.87, 21_240.54},
            {23, 1_000, 12_380.76, -3_407.42, 10_919.67},
            {24, 1_000, 12_380.76, -3_407.42, 11_798.15},
            {25, 500, 6_190.38, -3_084.86, 6_512.94},
            {26, 1_500, 18_223.29, -3_084.86, 18_049.37},
            {27, 1_500, 18_223.29, -3_084.86, 16_273.96},
            {28, 1_000, 12_148.86, -3_141.75, 12_035.10},
            {29, 1_000, 12_148.86, -3_141.75, 11_830.34},
            {30, 1_000, 12_148.86, -3_141.75, 14_655.06},
    };

    private final Map<LocalDate, Double> cotacoes = new HashMap<>();
    private final List<MovimentacaoDTO> movimentacoes = new ArrayList<>();
    private ApuracaoDTO apuracao;

    @BeforeEach
    void apurarPlanilha() throws AppException {
        for (double[] dia : DIAS) {
            cotacoes.put(dia((int) dia[0]), dia[4] / dia[1]);
        }
        int id = 1;
        for (Object[] linha : PLANILHA) {
            LocalDate data = dia((int) linha[0]);
            TipoMovimentacao tipo = (TipoMovimentacao) linha[1];
            double quantidade = tipo == TipoMovimentacao.COMPRA ? COMPRA : VENDA;
            cotacoes.put(data, (double) linha[2] / quantidade);
            movimentacoes.add(new MovimentacaoDTO(id++, 1, data, tipo, quantidade));
        }
        apuracao = ApuracaoCarteira.calcular(movimentacoes, cotacoes::get).toDTO(1, new OraculoDTO(dia(30), COTACAO_FINAL));
    }

    @Test
    @DisplayName("Cada linha com operação reproduz a posição e o resultado da planilha")
    void linhasReproduzemPlanilha() {
        List<ApuracaoDTO.Linha> linhas = apuracao.linhas();
        assertEquals(PLANILHA.length, linhas.size());

        for (int i = 0; i < PLANILHA.length; i++) {
            Object[] esperado = PLANILHA[i];
            ApuracaoDTO.Linha linha = linhas.get(i);
            String contexto = "dia " + esperado[0];
            assertAll(contexto,
                    () -> assertEquals(dia((int) esperado[0]), linha.data(), contexto),
                    () -> assertEquals(esperado[1], linha.tipo(), contexto),
                    () -> assertEquals((double) esperado[2], linha.valor(), TOLERANCIA_CENTAVO, contexto),
                    () -> assertEquals((int) esperado[3], linha.saldoQuantidade(), TOLERANCIA_CENTAVO, contexto),
                    () -> assertEquals((double) esperado[4], linha.custoMedio(), TOLERANCIA_CENTAVO, contexto),
                    () -> assertEquals((double) esperado[5], linha.saldoCustoMedio(), TOLERANCIA_ACUMULADA, contexto),
                    () -> assertEquals((double) esperado[6], linha.resultadoOperacao(), TOLERANCIA_ACUMULADA, contexto),
                    () -> assertEquals((double) esperado[7], linha.resultadoAcumulado(), TOLERANCIA_ACUMULADA, contexto));
        }
    }

    @Test
    @DisplayName("Totais da planilha: quantidades e valores comprados e vendidos")
    void totaisReproduzemPlanilha() {
        assertAll(
                () -> assertEquals(1_000, apuracao.saldoQuantidade(), TOLERANCIA_CENTAVO),
                () -> assertEquals(76_798.50, apuracao.totalComprado(), TOLERANCIA_ACUMULADA),
                () -> assertEquals(61_507.89, apuracao.totalVendido(), TOLERANCIA_ACUMULADA),
                () -> assertEquals(12.15, apuracao.custoMedio(), TOLERANCIA_CENTAVO),
                () -> assertEquals(12_148.86, apuracao.saldoCustoMedio(), TOLERANCIA_ACUMULADA),
                () -> assertEquals(14_655.06, apuracao.saldoMercado(), TOLERANCIA_CENTAVO));
    }

    @Test
    @DisplayName("Resumo da planilha: resultado operacional, geral e a mercado")
    void resumoReproduzPlanilha() {
        assertAll(
                () -> assertEquals(-15_290.61, apuracao.resultadoOperacional(), TOLERANCIA_ACUMULADA),
                () -> assertEquals(-3_141.75, apuracao.resultadoCustoMedio(), TOLERANCIA_ACUMULADA),
                () -> assertEquals(-4.09, apuracao.percentualCustoMedio(), TOLERANCIA_PERCENTUAL),
                () -> assertEquals(-635.55, apuracao.resultadoMercado(), TOLERANCIA_ACUMULADA),
                () -> assertEquals(-0.83, apuracao.percentualMercado(), TOLERANCIA_PERCENTUAL));
    }

    @Test
    @DisplayName("Evolução diária reproduz os 30 dias da planilha, inclusive os sem operação")
    void evolucaoDiariaReproduzPlanilha() throws AppException {
        List<ApuracaoDTO.Dia> dias = ApuracaoCarteira.evolucaoDiaria(movimentacoes, cotacoes::get, dia(30));
        assertEquals(DIAS.length, dias.size());

        for (int i = 0; i < DIAS.length; i++) {
            double[] esperado = DIAS[i];
            ApuracaoDTO.Dia dia = dias.get(i);
            String contexto = "dia " + (int) esperado[0];
            assertAll(contexto,
                    () -> assertEquals(dia((int) esperado[0]), dia.data(), contexto),
                    () -> assertEquals(esperado[1], dia.saldoQuantidade(), TOLERANCIA_CENTAVO, contexto),
                    () -> assertEquals(esperado[2], dia.saldoCustoMedio(), TOLERANCIA_ACUMULADA, contexto),
                    () -> assertEquals(esperado[3], dia.resultadoAcumulado(), TOLERANCIA_ACUMULADA, contexto),
                    () -> assertEquals(esperado[4], dia.saldoMercado(), TOLERANCIA_ACUMULADA, contexto));
        }
        assertAll("totais",
                () -> assertEquals(6_000, dias.stream().mapToDouble(ApuracaoDTO.Dia::quantidadeComprada).sum()),
                () -> assertEquals(5_000, dias.stream().mapToDouble(ApuracaoDTO.Dia::quantidadeVendida).sum()),
                () -> assertEquals(76_798.50, dias.stream().mapToDouble(ApuracaoDTO.Dia::valorComprado).sum(),
                        TOLERANCIA_ACUMULADA),
                () -> assertEquals(61_507.89, dias.stream().mapToDouble(ApuracaoDTO.Dia::valorVendido).sum(),
                        TOLERANCIA_ACUMULADA));
    }

    private static LocalDate dia(int dia) {
        return LocalDate.of(2026, 1, dia);
    }
}
