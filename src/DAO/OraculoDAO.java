package DAO;

import DTO.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;

public interface OraculoDAO {

    void incluir(OraculoDTO oraculo) throws AppException;

    OraculoDTO consultarPorData(LocalDate data) throws AppException;

    boolean existe(LocalDate data) throws AppException;
}
