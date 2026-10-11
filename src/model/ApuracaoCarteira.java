package model;

import DTO.ApuracaoDTO;
import DTO.LinhaApuracaoDTO;
import DTO.MovimentacaoDTO;
import exception.AppException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Apuração dos resultados de uma carteira a partir do seu histórico de movimentações.
 * <p>
 * As movimentações são percorridas em ordem cronológica (empates pela ordem de registro).
 * O custo médio é uma média ponderada recalculada apenas nas compras; as vendas reduzem a
 * quantidade mantendo o custo médio e geram o resultado da operação, igual à quantidade
 * vendida multiplicada pela diferença entre a cotação e o custo médio.
 * </p>
 */
public class ApuracaoCarteira {

    /** Tolerância para comparar quantidades em ponto flutuante. */
    private static final double TOLERANCIA = 1e-9;

    private static final double CEM_PORCENTO = 100.0;

    private final List<LinhaApuracaoDTO> linhas;
    private final double saldoQuantidade;
    private final double custoMedio;
    private final double totalComprado;
    private final double totalVendido;

    private ApuracaoCarteira(List<LinhaApuracaoDTO> linhas, double saldoQuantidade, double custoMedio,
                             double totalComprado, double totalVendido) {
        this.linhas = Collections.unmodifiableList(linhas);
        this.saldoQuantidade = saldoQuantidade;
        this.custoMedio = custoMedio;
        this.totalComprado = totalComprado;
        this.totalVendido = totalVendido;
    }

    /**
     * Apura uma carteira a partir de suas movimentações.
     *
     * @param movimentacoes movimentações da carteira, em qualquer ordem
     * @param cotacoes      fornecedor da cotação de cada data de movimentação
     * @return a apuração calculada
     * @throws AppException se uma cotação não existir, se uma movimentação for inválida
     *                      ou se uma venda exceder o saldo disponível na sua data
     */
    public static ApuracaoCarteira calcular(List<MovimentacaoDTO> movimentacoes, ProvedorCotacao cotacoes)
            throws AppException {
        List<MovimentacaoDTO> ordenadas = new ArrayList<>(movimentacoes);
        ordenadas.sort(Comparator.comparing(MovimentacaoDTO::getData)
                .thenComparingInt(MovimentacaoDTO::getIdMovimento));

        List<LinhaApuracaoDTO> linhas = new ArrayList<>();
        double saldo = 0;
        double custoMedio = 0;
        double totalComprado = 0;
        double totalVendido = 0;
        double resultadoAcumulado = 0;

        for (MovimentacaoDTO movimentacao : ordenadas) {
            double quantidade = movimentacao.getQuantidade();
            if (movimentacao.getTipo() == null || quantidade <= 0) {
                throw new AppException("Movimentação " + movimentacao.getIdMovimento()
                        + " possui tipo ou quantidade inválidos.");
            }

            double cotacao = cotacoes.cotacaoEm(movimentacao.getData());
            double valor = quantidade * cotacao;
            double resultadoOperacao = 0;

            if (movimentacao.getTipo() == TipoMovimentacao.COMPRA) {
                custoMedio = (custoMedio * saldo + valor) / (saldo + quantidade);
                saldo += quantidade;
                totalComprado += valor;
            } else {
                if (quantidade > saldo + TOLERANCIA) {
                    throw new AppException("Histórico inconsistente: a venda de " + quantidade
                            + " em " + movimentacao.getData() + " excede o saldo disponível (" + saldo + ").");
                }
                resultadoOperacao = quantidade * (cotacao - custoMedio);
                saldo -= quantidade;
                totalVendido += valor;
            }
            resultadoAcumulado += resultadoOperacao;

            if (saldo <= TOLERANCIA) {
                saldo = 0;
            }
            linhas.add(new LinhaApuracaoDTO(movimentacao.getData(), movimentacao.getTipo(), quantidade,
                    cotacao, valor, saldo, custoMedio, saldo * custoMedio, saldo * cotacao,
                    resultadoOperacao, resultadoAcumulado));

            if (saldo == 0) {
                custoMedio = 0;
            }
        }
        return new ApuracaoCarteira(linhas, saldo, custoMedio, totalComprado, totalVendido);
    }

    /** @return quantidade de FTCoins em carteira */
    public double getSaldoQuantidade() {
        return saldoQuantidade;
    }

    /** @return custo médio por FTCoin (zero quando não há saldo) */
    public double getCustoMedio() {
        return custoMedio;
    }

    /** @return soma dos valores de todas as compras */
    public double getTotalComprado() {
        return totalComprado;
    }

    /** @return soma dos valores de todas as vendas */
    public double getTotalVendido() {
        return totalVendido;
    }

    /** @return histórico apurado linha a linha, em ordem cronológica */
    public List<LinhaApuracaoDTO> getLinhas() {
        return linhas;
    }

    /** @return saldo em carteira valorado a custo médio */
    public double getSaldoCustoMedio() {
        return saldoQuantidade * custoMedio;
    }

    /** @return total vendido menos total comprado */
    public double getResultadoOperacional() {
        return totalVendido - totalComprado;
    }

    /** @return resultado operacional somado ao saldo valorado a custo médio */
    public double getResultadoCustoMedio() {
        return getResultadoOperacional() + getSaldoCustoMedio();
    }

    /**
     * Resultado caso todo o saldo fosse vendido à cotação informada.
     *
     * @param cotacaoAtual cotação usada para valorar o saldo
     * @return resultado operacional somado ao saldo valorado a mercado
     */
    public double getResultadoMercado(double cotacaoAtual) {
        return getResultadoOperacional() + saldoQuantidade * cotacaoAtual;
    }

    /**
     * Converte a apuração para o objeto de transporte usado pelas camadas superiores.
     *
     * @param idCarteira   identificador da carteira apurada
     * @param cotacaoAtual cotação usada na valoração a mercado
     * @return o DTO com a posição, os resultados e o histórico da carteira
     */
    public ApuracaoDTO toDTO(int idCarteira, double cotacaoAtual) {
        double resultadoCustoMedio = getResultadoCustoMedio();
        double resultadoMercado = getResultadoMercado(cotacaoAtual);
        return new ApuracaoDTO(idCarteira, cotacaoAtual, saldoQuantidade, custoMedio,
                getSaldoCustoMedio(), saldoQuantidade * cotacaoAtual, totalComprado, totalVendido,
                getResultadoOperacional(), resultadoCustoMedio, percentualSobreComprado(resultadoCustoMedio),
                resultadoMercado, percentualSobreComprado(resultadoMercado), linhas);
    }

    private double percentualSobreComprado(double resultado) {
        return totalComprado > 0 ? resultado / totalComprado * CEM_PORCENTO : 0;
    }
}
