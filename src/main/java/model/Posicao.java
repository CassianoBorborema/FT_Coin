package model;

import exception.AppException;

/**
 * Posição de uma carteira durante a apuração: quantidade de FTCoins, custo médio e os totais
 * acumulados até a última operação aplicada.
 * <p>
 * O custo médio é uma média ponderada recalculada apenas nas compras. As vendas reduzem a
 * quantidade mantendo o custo médio e geram resultado igual à quantidade vendida multiplicada
 * pela diferença entre a cotação e o custo médio.
 * </p>
 */
public class Posicao {

    /** Tolerância para comparar quantidades em ponto flutuante. */
    private static final double TOLERANCIA = 1e-9;

    private double quantidade;
    private double custoMedio;
    private double totalComprado;
    private double totalVendido;
    private double resultadoAcumulado;

    /**
     * Aplica uma compra, recalculando o custo médio.
     *
     * @param quantidade quantidade comprada
     * @param cotacao    cotação da compra
     */
    public void comprar(double quantidade, double cotacao) {
        double valor = quantidade * cotacao;
        custoMedio = (custoMedio * this.quantidade + valor) / (this.quantidade + quantidade);
        this.quantidade += quantidade;
        totalComprado += valor;
    }

    /**
     * Aplica uma venda, mantendo o custo médio.
     *
     * @param quantidade quantidade vendida
     * @param cotacao    cotação da venda
     * @return o resultado da venda em relação ao custo médio
     * @throws AppException se a quantidade exceder o saldo da posição
     */
    public double vender(double quantidade, double cotacao) throws AppException {
        if (quantidade > this.quantidade + TOLERANCIA) {
            throw new AppException("a venda de " + quantidade + " excede o saldo disponível (" + this.quantidade + ").");
        }
        double resultado = quantidade * (cotacao - custoMedio);
        this.quantidade -= quantidade;
        if (this.quantidade <= TOLERANCIA) {
            this.quantidade = 0;
        }
        totalVendido += quantidade * cotacao;
        resultadoAcumulado += resultado;
        return resultado;
    }

    /** @return quantidade de FTCoins na posição */
    public double getQuantidade() {
        return quantidade;
    }

    /** @return {@code true} se ainda há FTCoins na posição */
    public boolean temSaldo() {
        return quantidade > 0;
    }

    /**
     * @return custo médio vigente na última operação. Quando a posição é zerada ele é mantido,
     *         e a próxima compra o redefine.
     */
    public double getCustoMedio() {
        return custoMedio;
    }

    /** @return quantidade valorada a custo médio */
    public double getSaldoCustoMedio() {
        return quantidade * custoMedio;
    }

    /** @return soma dos valores de todas as compras */
    public double getTotalComprado() {
        return totalComprado;
    }

    /** @return soma dos valores de todas as vendas */
    public double getTotalVendido() {
        return totalVendido;
    }

    /** @return soma dos resultados das vendas */
    public double getResultadoAcumulado() {
        return resultadoAcumulado;
    }
}
