package controller;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.time.Clock;
import java.time.LocalDate;
import model.Movimentacao;
import model.ProvedorCotacao;
import model.TipoMovimentacao;

/**
 * Registro de compras e vendas de FTCoins. Toda movimentação é feita na data do sistema,
 * à cotação do dia informada pelo Oráculo.
 */
public class MovimentacaoController {

    private final MovimentacaoDAO movimentacaoDAO;
    private final ValidadorCarteira validadorCarteira;
    private final ProvedorCotacao cotacoes;
    private final Clock relogio;

    /**
     * @param movimentacaoDAO persistência das movimentações
     * @param carteiraDAO     persistência das carteiras, usada para validar a carteira
     * @param cotacoes        fonte das cotações (Oráculo)
     * @param relogio         relógio que define a data das movimentações
     */
    public MovimentacaoController(MovimentacaoDAO movimentacaoDAO, CarteiraDAO carteiraDAO,
                                  ProvedorCotacao cotacoes, Clock relogio) {
        this.movimentacaoDAO = movimentacaoDAO;
        this.validadorCarteira = new ValidadorCarteira(carteiraDAO);
        this.cotacoes = cotacoes;
        this.relogio = relogio;
    }

    public MovimentacaoDTO registrarCompra(int idCarteira, double quantidade) throws AppException {
        return registrar(idCarteira, quantidade, TipoMovimentacao.COMPRA);
    }

    public MovimentacaoDTO registrarVenda(int idCarteira, double quantidade) throws AppException {
        return registrar(idCarteira, quantidade, TipoMovimentacao.VENDA);
    }

    /**
     * @return a cotação de hoje, usada nas movimentações registradas agora
     * @throws AppException se o Oráculo não tiver a cotação de hoje
     */
    public double cotacaoDeHoje() throws AppException {
        return cotacoes.cotacaoEm(LocalDate.now(relogio));
    }

    /**
     * Verifica se a carteira existe. É público para que a view avise o usuário
     * logo após a digitação do identificador, sem esperar os demais dados.
     *
     * @param idCarteira identificador da carteira
     * @throws AppException se a carteira não estiver cadastrada
     */
    public void validarCarteiraExiste(int idCarteira) throws AppException {
        validadorCarteira.validarExistente(idCarteira);
    }

    private MovimentacaoDTO registrar(int idCarteira, double quantidade, TipoMovimentacao tipo)
            throws AppException {
        validadorCarteira.validarExistente(idCarteira);
        LocalDate hoje = LocalDate.now(relogio);
        cotacoes.cotacaoEm(hoje);

        Movimentacao movimentacao = new Movimentacao(0, idCarteira, hoje, tipo, quantidade);
        movimentacao.validar();
        if (tipo == TipoMovimentacao.VENDA) {
            validarSaldo(idCarteira, quantidade);
        }

        MovimentacaoDTO dto = movimentacao.toDTO();
        movimentacaoDAO.incluir(dto);
        return movimentacaoDAO.consultarPorId(dto.getIdMovimento());
    }

    private void validarSaldo(int idCarteira, double quantidade) throws AppException {
        double saldo = movimentacaoDAO.calcularSaldo(idCarteira);
        if (quantidade > saldo) {
            throw new AppException(
                    "Saldo insuficiente para venda. Saldo atual: " + saldo + ", quantidade solicitada: " + quantidade + "."
            );
        }
    }
}
