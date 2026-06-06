package control;

import DAO.CarteiraDAO;
import DAO.MovimentacaoDAO;
import DAO.OraculoDAO;
import DTO.MovimentacaoDTO;
import exception.AppException;
import model.Movimentacao;
import model.TipoMovimentacao;
import java.time.LocalDate;

public class MovimentacaoController {

    private final MovimentacaoDAO movimentacaoDAO;
    private final CarteiraDAO carteiraDAO;
    private final OraculoDAO oraculoDAO;

    public MovimentacaoController(MovimentacaoDAO movimentacaoDAO, CarteiraDAO carteiraDAO, OraculoDAO oraculoDAO) {
        this.movimentacaoDAO = movimentacaoDAO;
        this.carteiraDAO = carteiraDAO;
        this.oraculoDAO = oraculoDAO;
    }

    public MovimentacaoDTO registrarCompra(int idCarteira, LocalDate data, double quantidade) throws AppException {
        return registrar(idCarteira, data, quantidade, TipoMovimentacao.COMPRA);
    }

    public MovimentacaoDTO registrarVenda(int idCarteira, LocalDate data, double quantidade) throws AppException {
        validarSaldo(idCarteira, quantidade);
        return registrar(idCarteira, data, quantidade, TipoMovimentacao.VENDA);
    }

    private MovimentacaoDTO registrar(int idCarteira, LocalDate data, double quantidade, TipoMovimentacao tipo)
            throws AppException {
        validarCarteiraExiste(idCarteira);
        validarCotacaoNaData(data);

        MovimentacaoDTO dto = new MovimentacaoDTO(0, idCarteira, data, tipo, quantidade);
        Movimentacao movimentacao = Movimentacao.fromDTO(dto);
        movimentacao.validar();

        MovimentacaoDTO dtoParaSalvar = movimentacao.toDTO();
        movimentacaoDAO.incluir(dtoParaSalvar);
        return movimentacaoDAO.consultarPorId(dtoParaSalvar.getIdMovimento());
    }

    private void validarCarteiraExiste(int idCarteira) throws AppException {
        if (!carteiraDAO.existe(idCarteira)) {
            throw new AppException("Carteira com identificador " + idCarteira + " não encontrada.");
        }
    }

    private void validarCotacaoNaData(LocalDate data) throws AppException {
        oraculoDAO.consultarPorData(data);
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
