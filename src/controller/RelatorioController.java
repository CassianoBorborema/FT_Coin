package controller;

import DAO.CarteiraDAO;
import DAO.MovimentacaoDAO;
import DAO.OraculoDAO;
import DTO.CarteiraDTO;
import DTO.MovimentacaoDTO;
import exception.AppException;
import model.TipoMovimentacao;
import java.time.LocalDate;
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

    public List<CarteiraDTO> listarPorIdentificador() throws AppException {
        List<CarteiraDTO> carteiras = carteiraDAO.listarTodas();
        carteiras.sort(Comparator.comparingInt(CarteiraDTO::getIdentificador));
        return carteiras;
    }

    public List<CarteiraDTO> listarPorTitular() throws AppException {
        List<CarteiraDTO> carteiras = carteiraDAO.listarTodas();
        carteiras.sort(Comparator.comparing(
                CarteiraDTO::getNomeTitular, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));
        return carteiras;
    }

    public double calcularSaldo(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);
        return movimentacaoDAO.calcularSaldo(idCarteira);
    }

    public List<MovimentacaoDTO> historico(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);
        List<MovimentacaoDTO> movimentacoes = movimentacaoDAO.listarPorCarteira(idCarteira);
        movimentacoes.sort(Comparator.comparing(MovimentacaoDTO::getData));
        return movimentacoes;
    }

    public double ganhoPerda(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);

        double saldoAtual = movimentacaoDAO.calcularSaldo(idCarteira);
        double cotacaoHoje = cotacaoNaData(LocalDate.now());
        double resultado = saldoAtual * cotacaoHoje;

        for (MovimentacaoDTO movimentacao : movimentacaoDAO.listarPorCarteira(idCarteira)) {
            double valor = movimentacao.getQuantidade() * cotacaoNaData(movimentacao.getData());
            if (movimentacao.getTipo() == TipoMovimentacao.VENDA) {
                resultado += valor;
            } else if (movimentacao.getTipo() == TipoMovimentacao.COMPRA) {
                resultado -= valor;
            }
        }
        return resultado;
    }

    private double cotacaoNaData(LocalDate data) throws AppException {
        if (!oraculoDAO.existe(data)) {
            throw new AppException(
                    "Não há cotação cadastrada para a data " + data + ". Cadastre-a no menu Oráculo."
            );
        }
        return oraculoDAO.consultarPorData(data).getCotacao();
    }

    private void validarCarteiraExiste(int idCarteira) throws AppException {
        if (idCarteira <= 0) {
            throw new AppException("Identificador da carteira deve ser maior que zero.");
        }
        if (!carteiraDAO.existe(idCarteira)) {
            throw new AppException("Carteira com identificador " + idCarteira + " não encontrada.");
        }
    }
}
