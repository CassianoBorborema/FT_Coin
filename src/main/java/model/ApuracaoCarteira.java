package model;

import dto.ApuracaoDTO;
import dto.LinhaApuracaoDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Apuração de uma carteira: aplica as movimentações a uma {@link Posicao} em ordem cronológica
 * (empates pela ordem de registro) e gera uma linha de histórico por operação.
 */
public class ApuracaoCarteira {

    private static final double CEM_PORCENTO = 100.0;

    private final List<LinhaApuracaoDTO> linhas;
    private final double saldoQuantidade;
    private final double custoMedio;
    private final double totalComprado;
    private final double totalVendido;

    private ApuracaoCarteira(List<LinhaApuracaoDTO> linhas, Posicao posicao) {
        this.linhas = Collections.unmodifiableList(linhas);
        this.saldoQuantidade = posicao.getQuantidade();
        this.custoMedio = posicao.temSaldo() ? posicao.getCustoMedio() : 0;
        this.totalComprado = posicao.getTotalComprado();
        this.totalVendido = posicao.getTotalVendido();
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
        Posicao posicao = new Posicao();

        for (MovimentacaoDTO movimentacao : ordenadas) {
            double quantidade = movimentacao.getQuantidade();
            if (movimentacao.getTipo() == null || quantidade <= 0) {
                throw new AppException("Movimentação " + movimentacao.getIdMovimento()
                        + " possui tipo ou quantidade inválidos.");
            }

            double cotacao = cotacoes.cotacaoEm(movimentacao.getData());
            double resultadoOperacao;
            try {
                resultadoOperacao = movimentacao.getTipo().aplicar(posicao, quantidade, cotacao);
            } catch (AppException e) {
                throw new AppException("Histórico inconsistente em " + movimentacao.getData() + ": " + e.getMessage());
            }

            linhas.add(new LinhaApuracaoDTO(movimentacao.getData(), movimentacao.getTipo(), quantidade,
                    cotacao, quantidade * cotacao, posicao.getQuantidade(), posicao.getCustoMedio(),
                    posicao.getSaldoCustoMedio(), posicao.getQuantidade() * cotacao,
                    resultadoOperacao, posicao.getResultadoAcumulado()));
        }
        return new ApuracaoCarteira(linhas, posicao);
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
