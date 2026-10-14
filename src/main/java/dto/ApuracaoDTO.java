package dto;

import java.time.LocalDate;
import java.util.List;
import model.TipoMovimentacao;

/**
 * Resultado da apuração de uma carteira: posição atual, resultados nas valorações a custo médio e
 * a mercado e o histórico linha a linha. Os percentuais são calculados sobre o total comprado e
 * dados em pontos percentuais (ex.: -4.1 significa -4,1%).
 *
 * @param cotacaoAtual     última cotação disponível, usada na valoração a mercado
 * @param dataCotacaoAtual data dessa cotação
 */
public record ApuracaoDTO(int idCarteira, double cotacaoAtual, LocalDate dataCotacaoAtual, double saldoQuantidade,
                          double custoMedio, double saldoCustoMedio, double saldoMercado, double totalComprado,
                          double totalVendido, double resultadoOperacional, double resultadoCustoMedio,
                          double percentualCustoMedio, double resultadoMercado, double percentualMercado,
                          List<Linha> linhas) {

    public ApuracaoDTO {
        linhas = List.copyOf(linhas);
    }

    /**
     * Uma movimentação com a cotação do dia e a posição da carteira logo após a operação.
     * Nas compras, o custo médio já inclui a nova compra; nas vendas, o resultado da operação é
     * calculado sobre o custo médio vigente.
     */
    public record Linha(LocalDate data, TipoMovimentacao tipo, double quantidade, double cotacao, double valor,
                        double saldoQuantidade, double custoMedio, double saldoCustoMedio, double saldoMercado,
                        double resultadoOperacao, double resultadoAcumulado) {
    }

    /**
     * Posição da carteira ao fim de um dia, como uma linha da planilha de apuração. Nos dias sem
     * operação, quantidades e valores movimentados são zero e o saldo a mercado acompanha a cotação.
     */
    public record Dia(LocalDate data, double cotacao, double quantidadeComprada, double quantidadeVendida,
                      double valorComprado, double valorVendido, double saldoQuantidade, double custoMedio,
                      double saldoCustoMedio, double resultadoOperacao, double resultadoAcumulado,
                      double saldoMercado) {
    }
}
