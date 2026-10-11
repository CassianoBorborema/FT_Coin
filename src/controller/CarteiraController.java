package controller;

import DAO.CarteiraDAO;
import DAO.MovimentacaoDAO;
import DTO.CarteiraDTO;
import exception.AppException;
import model.Carteira;
import java.util.List;

public class CarteiraController {

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;

    public CarteiraController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
    }

    public CarteiraDTO incluir(String nomeTitular, String corretora) throws AppException {
        CarteiraDTO dto = new CarteiraDTO(0, nomeTitular, corretora);
        Carteira carteira = Carteira.fromDTO(dto);
        carteira.validar();
        CarteiraDTO dtoParaSalvar = carteira.toDTO();
        carteiraDAO.incluir(dtoParaSalvar);
        return carteiraDAO.consultarPorId(dtoParaSalvar.getIdentificador());
    }

    public CarteiraDTO consultar(int identificador) throws AppException {
        validarIdentificador(identificador);
        return carteiraDAO.consultarPorId(identificador);
    }

    public List<CarteiraDTO> listarTodas() throws AppException {
        return carteiraDAO.listarTodas();
    }

    public CarteiraDTO editar(int identificador, String nomeTitular, String corretora) throws AppException {
        validarIdentificador(identificador);
        CarteiraDTO dto = new CarteiraDTO(identificador, nomeTitular, corretora);
        Carteira carteira = Carteira.fromDTO(dto);
        carteira.validar();
        carteiraDAO.atualizar(carteira.toDTO());
        return carteiraDAO.consultarPorId(identificador);
    }

    public void excluir(int identificador) throws AppException {
        validarIdentificador(identificador);
        if (movimentacaoDAO.possuiMovimentacoes(identificador)) {
            throw new AppException(
                    "Não é possível excluir a carteira " + identificador + " pois existem movimentações vinculadas."
            );
        }
        carteiraDAO.excluir(identificador);
    }

    private void validarIdentificador(int identificador) throws AppException {
        if (identificador <= 0) {
            throw new AppException("Identificador da carteira deve ser maior que zero.");
        }
    }
}
