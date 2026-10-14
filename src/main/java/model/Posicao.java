package model;

import exception.AppException;

/**
 * Posição de uma carteira durante a apuração.
 * <p>
 * O custo médio é uma média ponderada recalculada só nas compras. A venda mantém o custo médio
 * e gera resultado igual a {@code quantidade × (cotação - custo médio)}.
 * </p>
 */
public class Posicao {

    private static final double TOLERANCIA = 1e-9;

    private double quantidade;
    private double custoMedio;
    private double quantidadeComprada;
    private double quantidadeVendida;
    private double totalComprado;
    private double totalVendido;
    private double resultadoAcumulado;

    public void comprar(double quantidade, double cotacao) {
        double valor = quantidade * cotacao;
        custoMedio = (custoMedio * this.quantidade + valor) / (this.quantidade + quantidade);
        this.quantidade += quantidade;
        quantidadeComprada += quantidade;
        totalComprado += valor;
    }

    /**
     * @return o resultado da venda em relação ao custo médio
     * @throws AppException se a quantidade exceder o saldo
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
        quantidadeVendida += quantidade;
        totalVendido += quantidade * cotacao;
        resultadoAcumulado += resultado;
        return resultado;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public boolean temSaldo() {
        return quantidade > 0;
    }

    /** Quando a posição é zerada o custo médio é mantido, e a próxima compra o redefine. */
    public double getCustoMedio() {
        return custoMedio;
    }

    public double getSaldoCustoMedio() {
        return quantidade * custoMedio;
    }

    public double getQuantidadeComprada() {
        return quantidadeComprada;
    }

    public double getQuantidadeVendida() {
        return quantidadeVendida;
    }

    public double getTotalComprado() {
        return totalComprado;
    }

    public double getTotalVendido() {
        return totalVendido;
    }

    public double getResultadoAcumulado() {
        return resultadoAcumulado;
    }
}
