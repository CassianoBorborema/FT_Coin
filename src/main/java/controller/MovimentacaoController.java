package controller;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.time.Clock;
import java.time.LocalDate;
import model.ApuracaoCarteira;
import model.Movimentacao;
import model.Posicao;
import model.ProvedorCotacao;
import model.TipoMovimentacao;

/**
 * Registro de compras, vendas, créditos e saques. Toda movimentação é feita na data do sistema,
 * à cotação do dia informada pelo Oráculo.
 */
public class MovimentacaoController {

    private final MovimentacaoDAO movimentacaoDAO;
    private final CarteiraDAO carteiraDAO;
    private final ProvedorCotacao cotacoes;
    private final Clock relogio;
    private final LocalDate vigenciaCredito;

    /**
     * @param vigenciaCredito a partir desta data as compras debitam o saldo em reais da carteira
     */
    public MovimentacaoController(MovimentacaoDAO movimentacaoDAO, CarteiraDAO carteiraDAO,
                                  ProvedorCotacao cotacoes, Clock relogio, LocalDate vigenciaCredito) {
        this.movimentacaoDAO = movimentacaoDAO;
        this.carteiraDAO = carteiraDAO;
        this.cotacoes = cotacoes;
        this.relogio = relogio;
        this.vigenciaCredito = vigenciaCredito;
    }

    public MovimentacaoDTO registrarCompra(int idCarteira, double quantidade) throws AppException {
        return registrar(idCarteira, quantidade, TipoMovimentacao.COMPRA);
    }

    public MovimentacaoDTO registrarVenda(int idCarteira, double quantidade) throws AppException {
        return registrar(idCarteira, quantidade, TipoMovimentacao.VENDA);
    }

    public MovimentacaoDTO creditar(int idCarteira, double valor) throws AppException {
        return registrar(idCarteira, valor, TipoMovimentacao.CREDITO);
    }

    public MovimentacaoDTO sacar(int idCarteira, double valor) throws AppException {
        return registrar(idCarteira, valor, TipoMovimentacao.SAQUE);
    }

    public double cotacaoDeHoje() throws AppException {
        return cotacoes.cotacaoEm(LocalDate.now(relogio));
    }

    public double saldoEmReais(int idCarteira) throws AppException {
        carteiraDAO.validarExistente(idCarteira);
        return posicaoAtual(idCarteira).getSaldoReais();
    }

    /**
     * Verifica se a carteira existe. É público para que a view avise o usuário
     * logo após a digitação do identificador, sem esperar os demais dados.
     *
     * @param idCarteira identificador da carteira
     * @throws AppException se a carteira não estiver cadastrada
     */
    public void validarCarteiraExiste(int idCarteira) throws AppException {
        carteiraDAO.validarExistente(idCarteira);
    }

    /**
     * A operação é aplicada à posição atual antes de ser gravada, então as regras são as mesmas da
     * apuração: venda limitada ao saldo de FTCoins, compra e saque limitados ao saldo em reais.
     */
    private MovimentacaoDTO registrar(int idCarteira, double quantidade, TipoMovimentacao tipo)
            throws AppException {
        carteiraDAO.validarExistente(idCarteira);
        LocalDate hoje = LocalDate.now(relogio);
        double cotacao = cotacoes.cotacaoEm(hoje);

        Movimentacao movimentacao = new Movimentacao(0, idCarteira, hoje, tipo, quantidade);
        movimentacao.validar();
        tipo.aplicar(posicaoAtual(idCarteira), hoje, quantidade, cotacao);

        MovimentacaoDTO dto = movimentacao.toDTO();
        movimentacaoDAO.incluir(dto);
        return movimentacaoDAO.consultarPorId(dto.getIdMovimento());
    }

    private Posicao posicaoAtual(int idCarteira) throws AppException {
        return ApuracaoCarteira.calcular(movimentacaoDAO.listarPorCarteira(idCarteira), cotacoes, vigenciaCredito)
                .getPosicao();
    }
}
