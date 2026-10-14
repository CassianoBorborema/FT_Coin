package dao;

import dto.CarteiraDTO;
import exception.AppException;
import java.util.List;

public sealed interface CarteiraDAO permits CarteiraDAOMemoria, CarteiraDAOMariaDB {

    void incluir(CarteiraDTO carteira) throws AppException;

    CarteiraDTO consultarPorId(int identificador) throws AppException;

    List<CarteiraDTO> listarTodas() throws AppException;

    void atualizar(CarteiraDTO carteira) throws AppException;

    void excluir(int identificador) throws AppException;

    boolean existe(int identificador) throws AppException;

    static void validarIdentificador(int identificador) throws AppException {
        if (identificador <= 0) {
            throw new AppException("Identificador da carteira deve ser maior que zero.");
        }
    }

    default void validarExistente(int identificador) throws AppException {
        validarIdentificador(identificador);
        if (!existe(identificador)) {
            throw new AppException("Carteira com identificador " + identificador + " não encontrada.");
        }
    }
}
