package model;

import dto.ApuracaoDTO;
import dto.MovimentacaoDTO;
import dto.OraculoDTO;
import exception.AppException;
import exception.PersistenciaException;
import java.time.LocalDate;
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

    private final List<ApuracaoDTO.Linha> linhas;
    private final Posicao posicao;
    private final int ultimoIdMovimento;
    private final double saldoQuantidade;
    private final double custoMedio;
    private final double totalComprado;
    private final double totalVendido;

    private ApuracaoCarteira(List<ApuracaoDTO.Linha> linhas, Posicao posicao, int ultimoIdMovimento) {
        this.linhas = Collections.unmodifiableList(linhas);
        this.posicao = posicao;
        this.ultimoIdMovimento = ultimoIdMovimento;
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
        return calcular(movimentacoes, cotacoes, LocalDate.MAX);
    }

    /**
     * @param vigenciaCredito a partir desta data as compras debitam o saldo em reais
     */
    public static ApuracaoCarteira calcular(List<MovimentacaoDTO> movimentacoes, ProvedorCotacao cotacoes,
                                            LocalDate vigenciaCredito) throws AppException {
        List<ApuracaoDTO.Linha> linhas = new ArrayList<>();
        Posicao posicao = new Posicao(vigenciaCredito);
        int ultimoId = 0;
        for (MovimentacaoDTO movimentacao : ordenar(movimentacoes)) {
            double cotacao = cotacoes.cotacaoEm(movimentacao.getData());
            double resultadoOperacao = aplicar(movimentacao, posicao, cotacao);
            linhas.add(new ApuracaoDTO.Linha(movimentacao.getData(), movimentacao.getTipo(),
                    movimentacao.getQuantidade(), cotacao,
                    movimentacao.getTipo().valor(movimentacao.getQuantidade(), cotacao),
                    posicao.getQuantidade(), posicao.getCustoMedio(), posicao.getSaldoCustoMedio(),
                    posicao.getQuantidade() * cotacao, resultadoOperacao, posicao.getResultadoAcumulado(),
                    posicao.getSaldoReais()));
            ultimoId = Math.max(ultimoId, movimentacao.getIdMovimento());
        }
        return new ApuracaoCarteira(linhas, posicao, ultimoId);
    }

    /**
     * Uma linha por dia, da primeira movimentação até {@code ate}, como na planilha de apuração.
     * Dias sem operação e sem cotação disponível ficam de fora.
     *
     * @throws AppException se faltar cotação num dia com operação ou o histórico for inconsistente
     */
    public static List<ApuracaoDTO.Dia> evolucaoDiaria(List<MovimentacaoDTO> movimentacoes,
                                                       ProvedorCotacao cotacoes, LocalDate ate)
            throws AppException {
        return evolucaoDiaria(movimentacoes, cotacoes, ate, LocalDate.MAX);
    }

    public static List<ApuracaoDTO.Dia> evolucaoDiaria(List<MovimentacaoDTO> movimentacoes,
                                                       ProvedorCotacao cotacoes, LocalDate ate,
                                                       LocalDate vigenciaCredito) throws AppException {
        List<MovimentacaoDTO> ordenadas = ordenar(movimentacoes);
        List<ApuracaoDTO.Dia> dias = new ArrayList<>();
        if (ordenadas.isEmpty()) {
            return dias;
        }
        Posicao posicao = new Posicao(vigenciaCredito);
        int proxima = 0;
        for (LocalDate dia = ordenadas.getFirst().getData(); !dia.isAfter(ate); dia = dia.plusDays(1)) {
            int primeiraDoDia = proxima;
            while (proxima < ordenadas.size() && ordenadas.get(proxima).getData().equals(dia)) {
                proxima++;
            }
            List<MovimentacaoDTO> doDia = ordenadas.subList(primeiraDoDia, proxima);
            Double cotacao = cotacaoSeHouver(cotacoes, dia);
            if (cotacao == null && doDia.isEmpty()) {
                continue;
            }
            if (cotacao == null) {
                cotacao = cotacoes.cotacaoEm(dia);
            }

            double quantidadeComprada = posicao.getQuantidadeComprada();
            double quantidadeVendida = posicao.getQuantidadeVendida();
            double valorComprado = posicao.getTotalComprado();
            double valorVendido = posicao.getTotalVendido();
            double resultadoDoDia = 0;
            for (MovimentacaoDTO movimentacao : doDia) {
                resultadoDoDia += aplicar(movimentacao, posicao, cotacao);
            }
            dias.add(new ApuracaoDTO.Dia(dia, cotacao,
                    posicao.getQuantidadeComprada() - quantidadeComprada,
                    posicao.getQuantidadeVendida() - quantidadeVendida,
                    posicao.getTotalComprado() - valorComprado,
                    posicao.getTotalVendido() - valorVendido,
                    posicao.getQuantidade(), posicao.getCustoMedio(), posicao.getSaldoCustoMedio(),
                    resultadoDoDia, posicao.getResultadoAcumulado(), posicao.getQuantidade() * cotacao));
        }
        return dias;
    }

    private static List<MovimentacaoDTO> ordenar(List<MovimentacaoDTO> movimentacoes) {
        List<MovimentacaoDTO> ordenadas = new ArrayList<>(movimentacoes);
        ordenadas.sort(Comparator.comparing(MovimentacaoDTO::getData)
                .thenComparingInt(MovimentacaoDTO::getIdMovimento));
        return ordenadas;
    }

    private static double aplicar(MovimentacaoDTO movimentacao, Posicao posicao, double cotacao)
            throws AppException {
        double quantidade = movimentacao.getQuantidade();
        if (movimentacao.getTipo() == null || quantidade <= 0) {
            throw new AppException("erro.movimentacao.invalida", movimentacao.getIdMovimento());
        }
        try {
            return movimentacao.getTipo().aplicar(posicao, movimentacao.getData(), quantidade, cotacao);
        } catch (AppException e) {
            throw new AppException(e, "erro.historico.inconsistente", movimentacao.getData(), e);
        }
    }

    private static Double cotacaoSeHouver(ProvedorCotacao cotacoes, LocalDate dia) throws AppException {
        try {
            return cotacoes.cotacaoEm(dia);
        } catch (PersistenciaException e) {
            throw e;
        } catch (AppException _) {
            return null;
        }
    }

    /**
     * Posição ao fim da apuração. Serve para validar uma nova operação aplicando-a sobre ela;
     * depois disso a apuração não deve mais ser usada.
     */
    public Posicao getPosicao() {
        return posicao;
    }

    /**
     * Indica se a apuração foi feita sobre estas mesmas movimentações. Como movimentações não são
     * alteradas nem excluídas e os identificadores só crescem, basta comparar quantidade e maior id.
     */
    public boolean correspondeA(List<MovimentacaoDTO> movimentacoes) {
        int maiorId = movimentacoes.stream().mapToInt(MovimentacaoDTO::getIdMovimento).max().orElse(0);
        return movimentacoes.size() == linhas.size() && maiorId == ultimoIdMovimento;
    }

    public double getSaldoQuantidade() {
        return saldoQuantidade;
    }

    /** Zero quando não há saldo. */
    public double getCustoMedio() {
        return custoMedio;
    }

    public double getTotalComprado() {
        return totalComprado;
    }

    public double getTotalVendido() {
        return totalVendido;
    }

    public List<ApuracaoDTO.Linha> getLinhas() {
        return linhas;
    }

    public double getSaldoCustoMedio() {
        return saldoQuantidade * custoMedio;
    }

    /** Total vendido menos total comprado. */
    public double getResultadoOperacional() {
        return totalVendido - totalComprado;
    }

    /** Resultado operacional somado ao saldo valorado a custo médio. */
    public double getResultadoCustoMedio() {
        return getResultadoOperacional() + getSaldoCustoMedio();
    }

    /** Resultado caso todo o saldo fosse vendido à cotação informada. */
    public double getResultadoMercado(double cotacaoAtual) {
        return getResultadoOperacional() + saldoQuantidade * cotacaoAtual;
    }

    /**
     * @param cotacaoAtual cotação usada na valoração a mercado, com a sua data
     */
    public ApuracaoDTO toDTO(int idCarteira, OraculoDTO cotacaoAtual) {
        double cotacao = cotacaoAtual.getCotacao();
        double resultadoCustoMedio = getResultadoCustoMedio();
        double resultadoMercado = getResultadoMercado(cotacao);
        return new ApuracaoDTO(idCarteira, cotacao, cotacaoAtual.getData(), saldoQuantidade, custoMedio,
                getSaldoCustoMedio(), saldoQuantidade * cotacao, totalComprado, totalVendido,
                getResultadoOperacional(), resultadoCustoMedio, percentualSobreComprado(resultadoCustoMedio),
                resultadoMercado, percentualSobreComprado(resultadoMercado), posicao.getSaldoReais(), linhas);
    }

    private double percentualSobreComprado(double resultado) {
        return totalComprado > 0 ? resultado / totalComprado * CEM_PORCENTO : 0;
    }
}
