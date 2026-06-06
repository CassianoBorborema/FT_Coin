package control;

import DAO.CarteiraDAO;
import DAO.MovimentacaoDAO;
import DAO.OraculoDAO;
import DTO.CarteiraDTO;
import DTO.MovimentacaoDTO;
import DTO.OraculoDTO;
import exception.AppException;
import model.TipoMovimentacao;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class RelatorioController {

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final OraculoDAO oraculoDAO;

    public RelatorioController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoDAO oraculoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
        this.oraculoDAO = oraculoDAO;
    }

    /** Relatório 1: lista todas as carteiras ordenadas pelo identificador (crescente). */
    public List<CarteiraDTO> listarCarteiraPorIdentificador() throws AppException {
        List<CarteiraDTO> lista = new ArrayList<>(carteiraDAO.listarTodas());
        lista.sort(Comparator.comparingInt(CarteiraDTO::getIdentificador));
        return lista;
    }

    /** Relatório 2: lista todas as carteiras ordenadas pelo nome do titular (A-Z, ignore case). */
    public List<CarteiraDTO> listarCarteiraPorNomeTitular() throws AppException {
        List<CarteiraDTO> lista = new ArrayList<>(carteiraDAO.listarTodas());
        lista.sort(Comparator.comparing(c -> c.getNomeTitular().toLowerCase()));
        return lista;
    }

    /** Relatório 3: saldo atual (em moeda virtual) de uma carteira. */
    public double consultarSaldo(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);
        return movimentacaoDAO.calcularSaldo(idCarteira);
    }

    /** Relatório 4: histórico de movimentações de uma carteira, ordenado por data. */
    public List<MovimentacaoDTO> listarHistoricoMovimentacao(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);
        List<MovimentacaoDTO> lista = new ArrayList<>(movimentacaoDAO.listarPorCarteira(idCarteira));
        lista.sort(Comparator.comparing(MovimentacaoDTO::getData));
        return lista;
    }

    /**
     * Relatório 5: ganho ou perda total de cada carteira em moeda real.
     *
     * Cálculo:
     *   Para cada movimentação, busca a cotação do oráculo na data da operação.
     *   Venda  → valor recebido  = quantidade × cotação  (positivo)
     *   Compra → custo pago      = quantidade × cotação  (negativo)
     *   resultado = soma de vendas − soma de compras
     *
     * Carteiras sem movimentação aparecem com resultado 0.
     * Se não houver cotação para a data de alguma movimentação, a entrada é ignorada
     * e um aviso é registrado no campo de observação.
     */
    public List<ResultadoCarteira> calcularGanhoPerdaPorCarteira() throws AppException {
        List<CarteiraDTO> carteiras = carteiraDAO.listarTodas();
        List<ResultadoCarteira> resultados = new ArrayList<>();

        for (CarteiraDTO carteira : carteiras) {
            List<MovimentacaoDTO> movimentacoes = movimentacaoDAO.listarPorCarteira(carteira.getIdentificador());
            double resultado = 0.0;
            List<String> avisos = new ArrayList<>();

            for (MovimentacaoDTO mov : movimentacoes) {
                try {
                    OraculoDTO cotacaoDTO = oraculoDAO.consultarPorData(mov.getData());
                    double valor = mov.getQuantidade() * cotacaoDTO.getCotacao();
                    if (mov.getTipo() == TipoMovimentacao.VENDA) {
                        resultado += valor;
                    } else {
                        resultado -= valor;
                    }
                } catch (AppException e) {
                    avisos.add("Sem cotação para " + mov.getData() + " (mov. #" + mov.getIdMovimento() + " ignorada)");
                }
            }

            resultados.add(new ResultadoCarteira(carteira, resultado, avisos));
        }

        resultados.sort(Comparator.comparingInt(r -> r.getCarteira().getIdentificador()));
        return resultados;
    }

    // -------------------------------------------------------------------------
    // Classe interna de resultado (Relatório 5)
    // -------------------------------------------------------------------------

    public static class ResultadoCarteira {
        private final CarteiraDTO carteira;
        private final double resultado;          // positivo = ganho, negativo = perda
        private final List<String> avisos;

        public ResultadoCarteira(CarteiraDTO carteira, double resultado, List<String> avisos) {
            this.carteira = carteira;
            this.resultado = resultado;
            this.avisos = avisos;
        }

        public CarteiraDTO getCarteira() { return carteira; }
        public double getResultado()     { return resultado; }
        public List<String> getAvisos()  { return avisos; }
        public boolean temGanho()        { return resultado > 0; }
        public boolean temPerda()        { return resultado < 0; }
        public boolean isNeutro()        { return resultado == 0; }
    }

    // -------------------------------------------------------------------------
    // Helpers privados
    // -------------------------------------------------------------------------

    private void validarCarteiraExiste(int idCarteira) throws AppException {
        if (!carteiraDAO.existe(idCarteira)) {
            throw new AppException("Carteira com identificador " + idCarteira + " não encontrada.");
        }
    }
}