package controller;

import dao.CarteiraDAO;
import dao.factory.DAOFactory;
import dto.CarteiraDTO;
import exception.AppException;
import model.Carteira;

import java.util.List;

/**
 * Controller responsável pelas regras de negócio da Carteira.
 * Nunca acessa o DAO diretamente — usa o DAOFactory.
 */
public class CarteiraController {

    private final CarteiraDAO carteiraDAO;

    public CarteiraController() {
        this.carteiraDAO = DAOFactory.getCarteiraDAO();
    }

    // ----------------------------------------------------------------
    // INCLUIR
    // ----------------------------------------------------------------

    /**
     * Valida e inclui uma nova carteira.
     *
     * @param dto dados vindos da View
     * @throws AppException se os dados forem inválidos ou ID já existir
     */
    public void incluirCarteira(CarteiraDTO dto) throws AppException {
        validarDTO(dto);

        // Verifica duplicidade de ID
        if (carteiraDAO.buscarPorId(dto.getId()) != null) {
            throw new AppException(
                "Já existe uma carteira com o identificador " + dto.getId() + "."
            );
        }

        Carteira carteira = new Carteira(
            dto.getId(),
            dto.getNome().trim(),
            dto.getCorretora().trim()
        );

        carteiraDAO.inserir(carteira);
    }

    // ----------------------------------------------------------------
    // CONSULTAR
    // ----------------------------------------------------------------

    /**
     * Busca uma carteira pelo ID.
     *
     * @param id identificador da carteira
     * @return CarteiraDTO com os dados encontrados
     * @throws AppException se a carteira não for encontrada
     */
    public CarteiraDTO consultarCarteira(int id) throws AppException {
        validarId(id);

        Carteira carteira = carteiraDAO.buscarPorId(id);
        if (carteira == null) {
            throw new AppException(
                "Carteira com identificador " + id + " não encontrada."
            );
        }

        return toDTO(carteira);
    }

    /**
     * Retorna todas as carteiras cadastradas.
     *
     * @return lista de CarteiraDTO
     * @throws AppException se ocorrer erro na consulta
     */
    public List<Carteira> listarCarteiras() throws AppException {
        List<Carteira> carteiras = carteiraDAO.listar();
        if (carteiras == null || carteiras.isEmpty()) {
            throw new AppException("Nenhuma carteira cadastrada.");
        }
        return carteiras;
    }

    // ----------------------------------------------------------------
    // EDITAR
    // ----------------------------------------------------------------

    /**
     * Valida e atualiza os dados de uma carteira existente.
     *
     * @param dto dados atualizados vindos da View
     * @throws AppException se os dados forem inválidos ou carteira não existir
     */
    public void editarCarteira(CarteiraDTO dto) throws AppException {
        validarDTO(dto);

        Carteira existente = carteiraDAO.buscarPorId(dto.getId());
        if (existente == null) {
            throw new AppException(
                "Carteira com identificador " + dto.getId() + " não encontrada."
            );
        }

        Carteira atualizada = new Carteira(
            dto.getId(),
            dto.getNome().trim(),
            dto.getCorretora().trim()
        );

        carteiraDAO.atualizar(atualizada);
    }

    // ----------------------------------------------------------------
    // EXCLUIR
    // ----------------------------------------------------------------

    /**
     * Valida e exclui uma carteira pelo ID.
     *
     * @param id identificador da carteira a excluir
     * @throws AppException se o ID for inválido ou carteira não existir
     */
    public void excluirCarteira(int id) throws AppException {
        validarId(id);

        Carteira existente = carteiraDAO.buscarPorId(id);
        if (existente == null) {
            throw new AppException(
                "Carteira com identificador " + id + " não encontrada."
            );
        }

        carteiraDAO.excluir(id);
    }

    // ----------------------------------------------------------------
    // HELPERS PRIVADOS
    // ----------------------------------------------------------------

    private void validarDTO(CarteiraDTO dto) throws AppException {
        if (dto == null) {
            throw new AppException("Dados da carteira não podem ser nulos.");
        }
        validarId(dto.getId());
        if (dto.getNome() == null || dto.getNome().trim().isEmpty()) {
            throw new AppException("O nome do titular é obrigatório.");
        }
        if (dto.getCorretora() == null || dto.getCorretora().trim().isEmpty()) {
            throw new AppException("A corretora é obrigatória.");
        }
    }

    private void validarId(int id) throws AppException {
        if (id <= 0) {
            throw new AppException("O identificador deve ser um número positivo.");
        }
    }

    private CarteiraDTO toDTO(Carteira carteira) {
        return new CarteiraDTO(
            carteira.getId(),
            carteira.getNome(),
            carteira.getCorretora()
        );
    }
}