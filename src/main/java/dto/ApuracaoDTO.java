package dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Resultado da apuração de uma carteira: posição atual (quantidade, custo médio, valor a mercado),
 * resultados nas duas valorações e o histórico linha a linha. Os percentuais são dados em pontos
 * percentuais (ex.: -4.1 significa -4,1%) e calculados sobre o total comprado.
 */
public class ApuracaoDTO {

    private final int idCarteira;
    private final double cotacaoAtual;
    private final double saldoQuantidade;
    private final double custoMedio;
    private final double saldoCustoMedio;
    private final double saldoMercado;
    private final double totalComprado;
    private final double totalVendido;
    private final double resultadoOperacional;
    private final double resultadoCustoMedio;
    private final double percentualCustoMedio;
    private final double resultadoMercado;
    private final double percentualMercado;
    private final List<LinhaApuracaoDTO> linhas;

    public ApuracaoDTO(int idCarteira, double cotacaoAtual, double saldoQuantidade, double custoMedio,
                       double saldoCustoMedio, double saldoMercado, double totalComprado,
                       double totalVendido, double resultadoOperacional, double resultadoCustoMedio,
                       double percentualCustoMedio, double resultadoMercado, double percentualMercado,
                       List<LinhaApuracaoDTO> linhas) {
        this.idCarteira = idCarteira;
        this.cotacaoAtual = cotacaoAtual;
        this.saldoQuantidade = saldoQuantidade;
        this.custoMedio = custoMedio;
        this.saldoCustoMedio = saldoCustoMedio;
        this.saldoMercado = saldoMercado;
        this.totalComprado = totalComprado;
        this.totalVendido = totalVendido;
        this.resultadoOperacional = resultadoOperacional;
        this.resultadoCustoMedio = resultadoCustoMedio;
        this.percentualCustoMedio = percentualCustoMedio;
        this.resultadoMercado = resultadoMercado;
        this.percentualMercado = percentualMercado;
        this.linhas = Collections.unmodifiableList(new ArrayList<>(linhas));
    }

    public int getIdCarteira() {
        return idCarteira;
    }

    /** @return cotação usada na valoração a mercado */
    public double getCotacaoAtual() {
        return cotacaoAtual;
    }

    /** @return quantidade de FTCoins em carteira */
    public double getSaldoQuantidade() {
        return saldoQuantidade;
    }

    /** @return custo médio por FTCoin (zero quando não há saldo) */
    public double getCustoMedio() {
        return custoMedio;
    }

    /** @return saldo valorado a custo médio */
    public double getSaldoCustoMedio() {
        return saldoCustoMedio;
    }

    /** @return saldo valorado pela cotação atual */
    public double getSaldoMercado() {
        return saldoMercado;
    }

    /** @return soma dos valores de todas as compras */
    public double getTotalComprado() {
        return totalComprado;
    }

    /** @return soma dos valores de todas as vendas */
    public double getTotalVendido() {
        return totalVendido;
    }

    /** @return total vendido menos total comprado */
    public double getResultadoOperacional() {
        return resultadoOperacional;
    }

    /** @return resultado operacional somado ao saldo valorado a custo médio */
    public double getResultadoCustoMedio() {
        return resultadoCustoMedio;
    }

    public double getPercentualCustoMedio() {
        return percentualCustoMedio;
    }

    /** @return resultado caso todo o saldo fosse vendido à cotação atual */
    public double getResultadoMercado() {
        return resultadoMercado;
    }

    public double getPercentualMercado() {
        return percentualMercado;
    }

    /** @return histórico da carteira em ordem cronológica (lista não modificável) */
    public List<LinhaApuracaoDTO> getLinhas() {
        return linhas;
    }
}
