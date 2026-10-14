package controller;

import dao.CarteiraDAO;
import exception.AppException;

/**
 * Validações de carteira compartilhadas pelos controllers.
 */
class ValidadorCarteira {

    private final CarteiraDAO carteiraDAO;

    ValidadorCarteira(CarteiraDAO carteiraDAO) {
        this.carteiraDAO = carteiraDAO;
    }

    static void validarIdentificador(int identificador) throws AppException {
        if (identificador <= 0) {
            throw new AppException("Identificador da carteira deve ser maior que zero.");
        }
    }

    void validarExistente(int identificador) throws AppException {
        validarIdentificador(identificador);
        if (!carteiraDAO.existe(identificador)) {
            throw new AppException("Carteira com identificador " + identificador + " não encontrada.");
        }
    }
}
