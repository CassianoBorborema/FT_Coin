package controller;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dto.CarteiraDTO;
import exception.AppException;
import model.Carteira;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cadastro de carteiras. Valida os dados pelo modelo antes de gravar e só exclui
 * carteiras sem movimentações.
 */
public class CarteiraController {

    private static final Logger log = LoggerFactory.getLogger(CarteiraController.class);

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
        log.info("Carteira {} incluída", dtoParaSalvar.getIdentificador());
        return carteiraDAO.consultarPorId(dtoParaSalvar.getIdentificador());
    }

    public CarteiraDTO consultar(int identificador) throws AppException {
        CarteiraDAO.validarIdentificador(identificador);
        return carteiraDAO.consultarPorId(identificador);
    }

    public List<CarteiraDTO> listarTodas() throws AppException {
        return carteiraDAO.listarTodas();
    }

    public CarteiraDTO editar(int identificador, String nomeTitular, String corretora) throws AppException {
        CarteiraDAO.validarIdentificador(identificador);
        CarteiraDTO dto = new CarteiraDTO(identificador, nomeTitular, corretora);
        Carteira carteira = Carteira.fromDTO(dto);
        carteira.validar();
        carteiraDAO.atualizar(carteira.toDTO());
        log.info("Carteira {} editada", identificador);
        return carteiraDAO.consultarPorId(identificador);
    }

    public void excluir(int identificador) throws AppException {
        CarteiraDAO.validarIdentificador(identificador);
        if (movimentacaoDAO.possuiMovimentacoes(identificador)) {
            throw new AppException("erro.carteira.comMovimentacoes", identificador);
        }
        carteiraDAO.excluir(identificador);
        log.info("Carteira {} excluída", identificador);
    }
}
