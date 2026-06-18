package controller;

import DAO.CarteiraDAO;
import DAO.MovimentacaoDAO;
import DAO.OraculoDAO;
import DTO.CarteiraDTO;
import DTO.MovimentacaoDTO;
import DTO.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import model.TipoMovimentacao;

public class RelatorioController {

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final OraculoDAO oraculoDAO;

    public RelatorioController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoDAO oraculoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
        this.oraculoDAO = oraculoDAO;
    }

    /** Retorna todas as carteiras ordenadas por identificador (crescente). */
    public List<CarteiraDTO> listarPorIdentificador() throws AppException {
        List<CarteiraDTO> lista = carteiraDAO.listarTodas();
        lista.sort(Comparator.comparingInt(CarteiraDTO::getIdentificador));
        return lista;
    }

    /** Retorna todas as carteiras ordenadas pelo nome do titular (alfabética, case-insensitive). */
    public List<CarteiraDTO> listarPorNomeTitular() throws AppException {
        List<CarteiraDTO> lista = carteiraDAO.listarTodas();
        lista.sort(Comparator.comparing(c -> c.getNomeTitular().toLowerCase()));
        return lista;
    }

    /** Retorna a quantidade atual de moedas virtuais da carteira informada. */
    public double calcularSaldo(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);
        return movimentacaoDAO.calcularSaldo(idCarteira);
    }

    /**
     * Retorna o valor atual das moedas em BRL usando a cotacao de hoje no Oraculo.
     * Retorna null se nao houver cotacao cadastrada para a data de hoje.
     */
    public Double calcularValorAtualBRL(int idCarteira) throws AppException {
        double saldo = calcularSaldo(idCarteira);
        try {
            OraculoDTO cotacaoHoje = oraculoDAO.consultarPorData(LocalDate.now());
            return saldo * cotacaoHoje.getCotacao();
        } catch (AppException e) {
            return null;
        }
    }

    /** Retorna o historico de movimentacoes de uma carteira, ordenado por data (crescente). */
    public List<MovimentacaoDTO> listarHistorico(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);
        List<MovimentacaoDTO> lista = movimentacaoDAO.listarPorCarteira(idCarteira);
        lista.sort(Comparator.comparing(MovimentacaoDTO::getData));
        return lista;
    }

    /**
     * Calcula o ganho ou perda total de cada carteira cadastrada, ordenadas por ID.
     *
     * Formula:
     *   Resultado = (soma de vendas x cotacao_na_data)
     *             - (soma de compras x cotacao_na_data)
     *             + (saldo_atual x cotacao_hoje)
     *
     * Movimentacoes em datas sem cotacao no Oraculo sao ignoradas e contabilizadas
     * em cotacoesAusentes, com aviso ao usuario na view.
     */
    public List<ResultadoGanhoPerda> calcularGanhoPerdaTodas() throws AppException {
        List<CarteiraDTO> carteiras = carteiraDAO.listarTodas();
        carteiras.sort(Comparator.comparingInt(CarteiraDTO::getIdentificador));

        List<ResultadoGanhoPerda> resultados = new ArrayList<>();
        for (CarteiraDTO carteira : carteiras) {
            resultados.add(calcularGanhoPerdaCarteira(carteira));
        }
        return resultados;
    }

    private ResultadoGanhoPerda calcularGanhoPerdaCarteira(CarteiraDTO carteira) throws AppException {
        List<MovimentacaoDTO> movimentacoes = movimentacaoDAO.listarPorCarteira(carteira.getIdentificador());

        double totalInvestido   = 0;
        double totalRecuperado  = 0;
        int cotacoesAusentes    = 0;

        for (MovimentacaoDTO mov : movimentacoes) {
            try {
                OraculoDTO cotacao = oraculoDAO.consultarPorData(mov.getData());
                double valorMovimento = mov.getQuantidade() * cotacao.getCotacao();
                if (mov.getTipo() == TipoMovimentacao.COMPRA) {
                    totalInvestido += valorMovimento;
                } else {
                    totalRecuperado += valorMovimento;
                }
            } catch (AppException e) {
                cotacoesAusentes++;
            }
        }

        double saldoAtual    = movimentacaoDAO.calcularSaldo(carteira.getIdentificador());
        double valorAtualBRL = 0;
        boolean temCotacaoHoje = false;

        try {
            OraculoDTO cotacaoHoje = oraculoDAO.consultarPorData(LocalDate.now());
            valorAtualBRL  = saldoAtual * cotacaoHoje.getCotacao();
            temCotacaoHoje = true;
        } catch (AppException e) {
            // sem cotacao para hoje; valorAtualBRL permanece 0 e nao entra no calculo
        }

        double ganhoPerda = totalRecuperado + valorAtualBRL - totalInvestido;

        return new ResultadoGanhoPerda(
                carteira, totalInvestido, totalRecuperado,
                saldoAtual, valorAtualBRL, ganhoPerda,
                temCotacaoHoje, cotacoesAusentes
        );
    }

    private void validarCarteiraExiste(int idCarteira) throws AppException {
        if (!carteiraDAO.existe(idCarteira)) {
            throw new AppException("Carteira com identificador " + idCarteira + " nao encontrada.");
        }
    }

    // -------------------------------------------------------------------------
    // Classe interna: encapsula o resultado de ganho/perda de uma carteira
    // -------------------------------------------------------------------------
    public static class ResultadoGanhoPerda {

        private final CarteiraDTO carteira;
        private final double totalInvestido;
        private final double totalRecuperado;
        private final double saldoAtualCoins;
        private final double valorAtualBRL;
        private final double ganhoPerda;
        private final boolean temCotacaoHoje;
        private final int cotacoesAusentes;

        public ResultadoGanhoPerda(CarteiraDTO carteira, double totalInvestido, double totalRecuperado,
                                   double saldoAtualCoins, double valorAtualBRL, double ganhoPerda,
                                   boolean temCotacaoHoje, int cotacoesAusentes) {
            this.carteira        = carteira;
            this.totalInvestido  = totalInvestido;
            this.totalRecuperado = totalRecuperado;
            this.saldoAtualCoins = saldoAtualCoins;
            this.valorAtualBRL   = valorAtualBRL;
            this.ganhoPerda      = ganhoPerda;
            this.temCotacaoHoje  = temCotacaoHoje;
            this.cotacoesAusentes = cotacoesAusentes;
        }

        public CarteiraDTO getCarteira()        { return carteira; }
        public double getTotalInvestido()        { return totalInvestido; }
        public double getTotalRecuperado()       { return totalRecuperado; }
        public double getSaldoAtualCoins()       { return saldoAtualCoins; }
        public double getValorAtualBRL()         { return valorAtualBRL; }
        public double getGanhoPerda()            { return ganhoPerda; }
        public boolean isTemCotacaoHoje()        { return temCotacaoHoje; }
        public int getCotacoesAusentes()         { return cotacoesAusentes; }
    }
}