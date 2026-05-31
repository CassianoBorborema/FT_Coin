package controller;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.OraculoDAO;
import dao.factory.DAOFactory;
import exception.AppException;
import model.Carteira;
import model.Movimentacao;
import model.Oraculo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

/**
 * Controller responsável pela geração dos relatórios do sistema.
 * Não repete lógica de movimentação — delega ao MovimentacaoController.
 */
public class RelatorioController {

    private final CarteiraDAO          carteiraDAO;
    private final MovimentacaoDAO      movimentacaoDAO;
    private final OraculoDAO           oraculoDAO;
    private final MovimentacaoController movimentacaoController;

    public RelatorioController() {
        this.carteiraDAO           = DAOFactory.getCarteiraDAO();
        this.movimentacaoDAO       = DAOFactory.getMovimentacaoDAO();
        this.oraculoDAO            = DAOFactory.getOraculoDAO();
        this.movimentacaoController = new MovimentacaoController();
    }

    // ----------------------------------------------------------------
    // RELATÓRIO 1 — Carteiras ordenadas por identificador
    // ----------------------------------------------------------------

    /**
     * Lista todas as carteiras ordenadas pelo identificador (crescente).
     *
     * @return lista de carteiras ordenada por ID
     * @throws AppException se não houver carteiras cadastradas
     */
    public List<Carteira> listarPorId() throws AppException {
        List<Carteira> carteiras = carteiraDAO.listar();
        if (carteiras == null || carteiras.isEmpty()) {
            throw new AppException("Nenhuma carteira cadastrada.");
        }
        carteiras.sort(Comparator.comparingInt(Carteira::getId));
        return carteiras;
    }

    // ----------------------------------------------------------------
    // RELATÓRIO 2 — Carteiras ordenadas por nome do titular
    // ----------------------------------------------------------------

    /**
     * Lista todas as carteiras ordenadas pelo nome do titular (A-Z).
     *
     * @return lista de carteiras ordenada por nome
     * @throws AppException se não houver carteiras cadastradas
     */
    public List<Carteira> listarPorNome() throws AppException {
        List<Carteira> carteiras = carteiraDAO.listar();
        if (carteiras == null || carteiras.isEmpty()) {
            throw new AppException("Nenhuma carteira cadastrada.");
        }
        carteiras.sort(Comparator.comparing(
            Carteira::getNome,
            String.CASE_INSENSITIVE_ORDER
        ));
        return carteiras;
    }

    // ----------------------------------------------------------------
    // RELATÓRIO 3 — Saldo atual de uma carteira (em moeda real)
    // ----------------------------------------------------------------

    /**
     * Exibe o saldo atual de uma carteira convertido para moeda real,
     * usando a cotação mais recente disponível no Oráculo.
     *
     * @param idCarteira identificador da carteira
     * @return saldo em moeda real (BigDecimal)
     * @throws AppException se carteira não existir ou cotação indisponível
     */
    public BigDecimal exibirSaldoEmReal(int idCarteira) throws AppException {
        garantirCarteiraExistente(idCarteira);

        BigDecimal saldoMoeda =
            movimentacaoController.calcularSaldoMoeda(idCarteira);

        Oraculo cotacao = oraculoDAO.buscarMaisRecente();
        if (cotacao == null) {
            throw new AppException(
                "Nenhuma cotação disponível no Oráculo para calcular o saldo."
            );
        }

        return saldoMoeda
            .multiply(cotacao.getCotacao())
            .setScale(2, RoundingMode.HALF_UP);
    }

    // ----------------------------------------------------------------
    // RELATÓRIO 4 — Histórico de movimentação de uma carteira
    // ----------------------------------------------------------------

    /**
     * Retorna o histórico completo de movimentações de uma carteira.
     * Delega ao MovimentacaoController.
     *
     * @param idCarteira identificador da carteira
     * @return lista de movimentações
     * @throws AppException se carteira não existir ou não houver movimentações
     */
    public List<Movimentacao> exibirHistorico(int idCarteira) throws AppException {
        return movimentacaoController.listarHistorico(idCarteira);
    }

    // ----------------------------------------------------------------
    // RELATÓRIO 5 — Ganho ou perda total de cada carteira
    // ----------------------------------------------------------------

    /**
     * Resultado financeiro de uma carteira: diferença entre o valor
     * atual da moeda em estoque e o custo total de aquisição.
     *
     * Fórmula:
     *   Ganho/Perda = (saldo_moeda × cotacao_hoje)
     *                 − Σ(compras × cotacao_na_data)
     *                 + Σ(vendas × cotacao_na_data)
     *
     * @param idCarteira identificador da carteira
     * @return valor positivo = ganho | negativo = perda
     * @throws AppException se carteira não existir ou cotação indisponível
     */
    public BigDecimal calcularGanhoPerdaTotal(int idCarteira) throws AppException {
        garantirCarteiraExistente(idCarteira);

        List<Movimentacao> movimentacoes =
            movimentacaoDAO.listarPorCarteira(idCarteira);

        if (movimentacoes == null || movimentacoes.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Custo líquido: compras aumentam, vendas realizam lucro/prejuízo
        BigDecimal custoTotal  = BigDecimal.ZERO;
        BigDecimal saldoMoeda  = BigDecimal.ZERO;

        for (Movimentacao m : movimentacoes) {
            BigDecimal valor = m.getQuantidade()
                .multiply(m.getCotacaoNaData())
                .setScale(2, RoundingMode.HALF_UP);

            if (m.getTipo() == 'C') {
                custoTotal = custoTotal.add(valor);
                saldoMoeda = saldoMoeda.add(m.getQuantidade());
            } else if (m.getTipo() == 'V') {
                custoTotal = custoTotal.subtract(valor);
                saldoMoeda = saldoMoeda.subtract(m.getQuantidade());
            }
        }

        // Valor atual do saldo remanescente
        Oraculo cotacaoAtual = oraculoDAO.buscarMaisRecente();
        if (cotacaoAtual == null) {
            throw new AppException(
                "Cotação atual não disponível para calcular ganho/perda."
            );
        }

        BigDecimal valorAtual = saldoMoeda
            .multiply(cotacaoAtual.getCotacao())
            .setScale(2, RoundingMode.HALF_UP);

        // Ganho/Perda = valor atual do estoque − custo líquido investido
        return valorAtual.subtract(custoTotal);
    }

    /**
     * Retorna o ganho/perda de TODAS as carteiras cadastradas.
     * A View pode usar este método para montar o relatório consolidado.
     *
     * @return lista de ResultadoCarteira com id, nome e ganho/perda
     * @throws AppException se não houver carteiras ou cotação indisponível
     */
    public List<ResultadoCarteira> calcularGanhoPerdaTodas() throws AppException {
        List<Carteira> carteiras = carteiraDAO.listar();
        if (carteiras == null || carteiras.isEmpty()) {
            throw new AppException("Nenhuma carteira cadastrada.");
        }

        List<ResultadoCarteira> resultados =
            new java.util.ArrayList<>();

        for (Carteira c : carteiras) {
            BigDecimal resultado = calcularGanhoPerdaTotal(c.getId());
            resultados.add(new ResultadoCarteira(
                c.getId(),
                c.getNome(),
                c.getCorretora(),
                resultado
            ));
        }

        return resultados;
    }

    // ----------------------------------------------------------------
    // INNER CLASS — Resultado por carteira (usado no Relatório 5)
    // ----------------------------------------------------------------

    /**
     * Classe auxiliar que agrega os dados de resultado por carteira.
     * Evita criar um DTO separado para um uso tão específico.
     */
    public static class ResultadoCarteira {

        private final int        id;
        private final String     nome;
        private final String     corretora;
        private final BigDecimal ganhoPerdaTotal;

        public ResultadoCarteira(
            int id,
            String nome,
            String corretora,
            BigDecimal ganhoPerdaTotal
        ) {
            this.id             = id;
            this.nome           = nome;
            this.corretora      = corretora;
            this.ganhoPerdaTotal = ganhoPerdaTotal;
        }

        public int getId()                   { return id; }
        public String getNome()              { return nome; }
        public String getCorretora()         { return corretora; }
        public BigDecimal getGanhoPerdaTotal() { return ganhoPerdaTotal; }

        /** Retorna true se houve ganho (resultado positivo). */
        public boolean isLucro() {
            return ganhoPerdaTotal.compareTo(BigDecimal.ZERO) >= 0;
        }
    }

    // ----------------------------------------------------------------
    // HELPERS PRIVADOS
    // ----------------------------------------------------------------

    private void garantirCarteiraExistente(int idCarteira) throws AppException {
        if (carteiraDAO.buscarPorId(idCarteira) == null) {
            throw new AppException(
                "Carteira com identificador " + idCarteira + " não encontrada."
            );
        }
    }
}