package DTO;

import model.TipoMovimentacao;
import java.time.LocalDate;

/**
 * Linha do histórico apurado de uma carteira: uma movimentação com a cotação do dia
 * e a posição da carteira (quantidade, custo médio e resultados) logo após a operação.
 */
public class LinhaApuracaoDTO {

    private final LocalDate data;
    private final TipoMovimentacao tipo;
    private final double quantidade;
    private final double cotacao;
    private final double valor;
    private final double saldoQuantidade;
    private final double custoMedio;
    private final double saldoCustoMedio;
    private final double saldoMercado;
    private final double resultadoOperacao;
    private final double resultadoAcumulado;

    public LinhaApuracaoDTO(LocalDate data, TipoMovimentacao tipo, double quantidade, double cotacao,
                            double valor, double saldoQuantidade, double custoMedio,
                            double saldoCustoMedio, double saldoMercado,
                            double resultadoOperacao, double resultadoAcumulado) {
        this.data = data;
        this.tipo = tipo;
        this.quantidade = quantidade;
        this.cotacao = cotacao;
        this.valor = valor;
        this.saldoQuantidade = saldoQuantidade;
        this.custoMedio = custoMedio;
        this.saldoCustoMedio = saldoCustoMedio;
        this.saldoMercado = saldoMercado;
        this.resultadoOperacao = resultadoOperacao;
        this.resultadoAcumulado = resultadoAcumulado;
    }

    public LocalDate getData() {
        return data;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    /** @return quantidade de FTCoins movimentada */
    public double getQuantidade() {
        return quantidade;
    }

    /** @return cotação da moeda na data da operação */
    public double getCotacao() {
        return cotacao;
    }

    /** @return valor financeiro da operação (quantidade x cotação) */
    public double getValor() {
        return valor;
    }

    /** @return quantidade de FTCoins em carteira após a operação */
    public double getSaldoQuantidade() {
        return saldoQuantidade;
    }

    /** @return custo médio vigente na operação (nas compras, já com a nova compra incluída) */
    public double getCustoMedio() {
        return custoMedio;
    }

    /** @return saldo valorado a custo médio após a operação */
    public double getSaldoCustoMedio() {
        return saldoCustoMedio;
    }

    /** @return saldo valorado pela cotação do dia da operação */
    public double getSaldoMercado() {
        return saldoMercado;
    }

    /** @return resultado da venda em relação ao custo médio (zero nas compras) */
    public double getResultadoOperacao() {
        return resultadoOperacao;
    }

    /** @return soma dos resultados das vendas até esta operação */
    public double getResultadoAcumulado() {
        return resultadoAcumulado;
    }
}
