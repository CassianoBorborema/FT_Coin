package model;

import exception.AppException;
import java.time.LocalDate;

/**
 * Posição de uma carteira durante a apuração: FTCoins, custo médio e saldo em reais.
 * <p>
 * O custo médio é uma média ponderada recalculada só nas compras. A venda mantém o custo médio
 * e gera resultado igual a {@code quantidade × (cotação - custo médio)}.
 * </p>
 * <p>
 * Saldo em reais: créditos e vendas somam, saques subtraem. Compras só debitam (e exigem saldo)
 * a partir da data de vigência do crédito; compras anteriores a ela não mexem no saldo em reais.
 * </p>
 */
public class Posicao {

    private static final double TOLERANCIA = 1e-9;

    private final LocalDate vigenciaCredito;

    private double quantidade;
    private double custoMedio;
    private double quantidadeComprada;
    private double quantidadeVendida;
    private double totalComprado;
    private double totalVendido;
    private double resultadoAcumulado;
    private double saldoReais;

    /** Posição em que nenhuma compra debita o saldo em reais. */
    public Posicao() {
        this(LocalDate.MAX);
    }

    public Posicao(LocalDate vigenciaCredito) {
        this.vigenciaCredito = vigenciaCredito;
    }

    /**
     * @throws AppException se a compra estiver na vigência do crédito e o saldo em reais não cobrir o valor
     */
    public void comprar(LocalDate data, double quantidade, double cotacao) throws AppException {
        double valor = quantidade * cotacao;
        if (!data.isBefore(vigenciaCredito)) {
            exigirSaldoReais(valor, "erro.saldoReais.compra");
            saldoReais -= valor;
        }
        custoMedio = (custoMedio * this.quantidade + valor) / (this.quantidade + quantidade);
        this.quantidade += quantidade;
        quantidadeComprada += quantidade;
        totalComprado += valor;
    }

    /**
     * @return o resultado da venda em relação ao custo médio
     * @throws AppException se a quantidade exceder o saldo de FTCoins
     */
    public double vender(double quantidade, double cotacao) throws AppException {
        if (quantidade > this.quantidade + TOLERANCIA) {
            throw new AppException("erro.venda.excedeSaldo", quantidade, this.quantidade);
        }
        double valor = quantidade * cotacao;
        double resultado = quantidade * (cotacao - custoMedio);
        this.quantidade -= quantidade;
        if (this.quantidade <= TOLERANCIA) {
            this.quantidade = 0;
        }
        quantidadeVendida += quantidade;
        totalVendido += valor;
        resultadoAcumulado += resultado;
        saldoReais += valor;
        return resultado;
    }

    public void creditar(double valor) {
        saldoReais += valor;
    }

    /** @throws AppException se o saldo em reais não cobrir o saque */
    public void sacar(double valor) throws AppException {
        exigirSaldoReais(valor, "erro.saldoReais.saque");
        saldoReais -= valor;
    }

    private void exigirSaldoReais(double valor, String chave) throws AppException {
        if (valor > saldoReais + TOLERANCIA) {
            throw new AppException(chave, saldoReais, valor);
        }
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

    public double getSaldoReais() {
        return saldoReais;
    }
}
