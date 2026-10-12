package controller;

import dao.CarteiraDAO;
import exception.AppException;

/**
 * Validações de identificador e existência de carteira usadas pelos controllers.
 */
class ValidadorCarteira {

    private final CarteiraDAO carteiraDAO;

    ValidadorCarteira(CarteiraDAO carteiraDAO) {
        this.carteiraDAO = carteiraDAO;
    }

    /**
     * @param identificador identificador da carteira
     * @throws AppException se o identificador não for positivo
     */
    static void validarIdentificador(int identificador) throws AppException {
        if (identificador <= 0) {
            throw new AppException("Identificador da carteira deve ser maior que zero.");
        }
    }

    /**
     * @param identificador identificador da carteira
     * @throws AppException se o identificador for inválido ou a carteira não estiver cadastrada
     */
    void validarExistente(int identificador) throws AppException {
        validarIdentificador(identificador);
        if (!carteiraDAO.existe(identificador)) {
            throw new AppException("Carteira com identificador " + identificador + " não encontrada.");
        }
    }
}
