package dao;

import dto.CarteiraDTO;
import exception.AppException;
import java.util.List;

/**
 * Persistência das carteiras. As implementações recebem e devolvem cópias dos DTOs.
 */
public sealed interface CarteiraDAO permits CarteiraDAOMemoria, CarteiraDAOMariaDB {

    /**
     * Com identificador zero, um identificador novo é gerado e gravado de volta em {@code carteira}.
     *
     * @throws AppException se o identificador informado já estiver em uso
     */
    void incluir(CarteiraDTO carteira) throws AppException;

    /** @throws AppException se a carteira não existir */
    CarteiraDTO consultarPorId(int identificador) throws AppException;

    List<CarteiraDTO> listarTodas() throws AppException;

    /** @throws AppException se a carteira não existir */
    void atualizar(CarteiraDTO carteira) throws AppException;

    /** @throws AppException se a carteira não existir */
    void excluir(int identificador) throws AppException;

    boolean existe(int identificador) throws AppException;

    static void validarIdentificador(int identificador) throws AppException {
        if (identificador <= 0) {
            throw new AppException("erro.carteira.identificador");
        }
    }

    /** @throws AppException se o identificador for inválido ou a carteira não existir */
    default void validarExistente(int identificador) throws AppException {
        validarIdentificador(identificador);
        if (!existe(identificador)) {
            throw new AppException("erro.carteira.naoEncontrada", identificador);
        }
    }
}
