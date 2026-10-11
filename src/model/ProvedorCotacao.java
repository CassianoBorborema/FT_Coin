package model;

import exception.AppException;
import java.time.LocalDate;

/**
 * Fornece a cotação da moeda virtual em uma data.
 * <p>
 * Isola o cálculo de apuração da origem da cotação (hoje o Oráculo),
 * o que permite trocar essa origem ou simulá-la em testes.
 * </p>
 */
@FunctionalInterface
public interface ProvedorCotacao {

    /**
     * Informa a cotação da moeda em uma data.
     *
     * @param data data da cotação desejada
     * @return a cotação da moeda na data informada
     * @throws AppException se não houver cotação para a data
     */
    double cotacaoEm(LocalDate data) throws AppException;
}
