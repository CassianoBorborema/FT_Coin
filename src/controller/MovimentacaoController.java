package controller;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.OraculoDAO;
import dao.factory.DAOFactory;
import dto.MovimentacaoDTO;
import exception.AppException;
import model.Carteira;
import model.Movimentacao;
import model.Oraculo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller responsável pelas regras de negócio de Movimentação.
 * Gerencia compra e venda de moeda virtual, consultando o Oráculo
 * para obter a cotação do dia.
 */
public class MovimentacaoController {

    private final MovimentacaoDAO movimentacaoDAO;
    private final CarteiraDAO     carteiraDAO;
    private final OraculoDAO      oraculoDAO;

    public MovimentacaoController() {
        this.movimentacaoDAO = DAOFactory.getMovimentacaoDAO();
        this.carteiraDAO     = DAOFactory.getCarteiraDAO();
        this.oraculoDAO      = DAOFactory.getOraculoDAO();
    }

    // ----------------------------------------------------------------
    // COMPRA
    // ----------------------------------------------------------------

    /**
     * Registra uma compra de moeda virtual para uma carteira.
     *
     * @param dto dados da movimentação (idCarteira, quantidade)
     * @throws AppException se dados inválidos, carteira inexistente
     *                      ou cotação indisponível
     */
    public void comprarMoeda(MovimentacaoDTO dto) throws AppException {
        validarMovimentacao(dto);

        garantirCarteiraExistente(dto.getIdCarteira());

        Oraculo cotacaoHoje = obterCotacaoHoje();

        Movimentacao movimentacao = new Movimentacao(
            dto.getIdMovimento(),
            dto.getIdCarteira(),
            LocalDate.now(),
            'C',
            dto.getQuantidade(),
            cotacaoHoje.getCotacao()
        );

        movimentacaoDAO.inserir(movimentacao);
    }

    // ----------------------------------------------------------------
    // VENDA
    // ----------------------------------------------------------------

    /**
     * Registra uma venda de moeda virtual de uma carteira.
     * Verifica se a carteira possui saldo suficiente para a venda.
     *
     * @param dto dados da movimentação (idCarteira, quantidade)
     * @throws AppException se dados inválidos, carteira inexistente,
     *                      saldo insuficiente ou cotação indisponível
     */
    public void venderMoeda(MovimentacaoDTO dto) throws AppException {
        validarMovimentacao(dto);

        garantirCarteiraExistente(dto.getIdCarteira());

        // Verifica saldo disponível
        BigDecimal saldo = calcularSaldoMoeda(dto.getIdCarteira());
        if (saldo.compareTo(dto.getQuantidade()) < 0) {
            throw new AppException(
                "Saldo insuficiente. Disponível: " + saldo + " | Solicitado: " + dto.getQuantidade()
            );
        }

        Oraculo cotacaoHoje = obterCotacaoHoje();

        Movimentacao movimentacao = new Movimentacao(
            dto.getIdMovimento(),
            dto.getIdCarteira(),
            LocalDate.now(),
            'V',
            dto.getQuantidade(),
            cotacaoHoje.getCotacao()
        );

        movimentacaoDAO.inserir(movimentacao);
    }

    // ----------------------------------------------------------------
    // CONSULTAS DE SUPORTE (usadas pelo RelatorioController também)
    // ----------------------------------------------------------------

    /**
     * Calcula o saldo atual em moeda virtual de uma carteira.
     * Compras somam, vendas subtraem.
     *
     * @param idCarteira identificador da carteira
     * @return saldo em moeda virtual
     * @throws AppException se ocorrer erro na consulta
     */
    public BigDecimal calcularSaldoMoeda(int idCarteira) throws AppException {
        List<Movimentacao> movimentacoes =
            movimentacaoDAO.listarPorCarteira(idCarteira);

        BigDecimal saldo = BigDecimal.ZERO;
        for (Movimentacao m : movimentacoes) {
            if (m.getTipo() == 'C') {
                saldo = saldo.add(m.getQuantidade());
            } else if (m.getTipo() == 'V') {
                saldo = saldo.subtract(m.getQuantidade());
            }
        }
        return saldo;
    }

    /**
     * Retorna o histórico completo de movimentações de uma carteira.
     *
     * @param idCarteira identificador da carteira
     * @return lista de movimentações ordenadas por data
     * @throws AppException se carteira não existir ou não houver movimentações
     */
    public List<Movimentacao> listarHistorico(int idCarteira) throws AppException {
        garantirCarteiraExistente(idCarteira);

        List<Movimentacao> historico =
            movimentacaoDAO.listarPorCarteira(idCarteira);

        if (historico == null || historico.isEmpty()) {
            throw new AppException(
                "Nenhuma movimentação encontrada para a carteira " + idCarteira + "."
            );
        }
        return historico;
    }

    // ----------------------------------------------------------------
    // HELPERS PRIVADOS
    // ----------------------------------------------------------------

    private void validarMovimentacao(MovimentacaoDTO dto) throws AppException {
        if (dto == null) {
            throw new AppException("Dados de movimentação não podem ser nulos.");
        }
        if (dto.getIdCarteira() <= 0) {
            throw new AppException("Identificador de carteira inválido.");
        }
        if (dto.getQuantidade() == null || dto.getQuantidade().compareTo(BigDecimal.ZERO) <= 0) {
            throw new AppException("A quantidade deve ser um valor positivo.");
        }
    }

    private void garantirCarteiraExistente(int idCarteira) throws AppException {
        Carteira carteira = carteiraDAO.buscarPorId(idCarteira);
        if (carteira == null) {
            throw new AppException(
                "Carteira com identificador " + idCarteira + " não encontrada."
            );
        }
    }

    private Oraculo obterCotacaoHoje() throws AppException {
        Oraculo cotacao = oraculoDAO.buscarPorData(LocalDate.now());
        if (cotacao == null) {
            throw new AppException(
                "Cotação do dia não disponível. Operação não pode ser realizada."
            );
        }
        return cotacao;
    }
}