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
    private final CarteiraDAO carteiraDAO;
    private final ProvedorCotacao cotacoes;
    private final Clock relogio;

    public MovimentacaoController(MovimentacaoDAO movimentacaoDAO, CarteiraDAO carteiraDAO,
                                  ProvedorCotacao cotacoes, Clock relogio) {
        this.movimentacaoDAO = movimentacaoDAO;
        this.carteiraDAO = carteiraDAO;
        this.cotacoes = cotacoes;
        this.relogio = relogio;
    }

    public MovimentacaoDTO registrarCompra(int idCarteira, double quantidade) throws AppException {
        return registrar(idCarteira, quantidade, TipoMovimentacao.COMPRA);
    }

    public MovimentacaoDTO registrarVenda(int idCarteira, double quantidade) throws AppException {
        return registrar(idCarteira, quantidade, TipoMovimentacao.VENDA);
    }

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
        carteiraDAO.validarExistente(idCarteira);
    }

    private MovimentacaoDTO registrar(int idCarteira, double quantidade, TipoMovimentacao tipo)
            throws AppException {
        carteiraDAO.validarExistente(idCarteira);
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
