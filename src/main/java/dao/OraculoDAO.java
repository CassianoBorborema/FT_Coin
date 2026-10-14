package dao;

import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;

/**
 * Histórico das cotações já usadas, uma por data.
 */
public sealed interface OraculoDAO permits OraculoDAOMemoria, OraculoDAOMariaDB {

    /** @throws AppException se a cotação for inválida ou já houver cotação na data */
    void incluir(OraculoDTO oraculo) throws AppException;

    /** @throws AppException se não houver cotação na data */
    OraculoDTO consultarPorData(LocalDate data) throws AppException;

    /** @return as cotações em ordem de data */
    List<OraculoDTO> listarTodas() throws AppException;

    boolean existe(LocalDate data) throws AppException;
}
