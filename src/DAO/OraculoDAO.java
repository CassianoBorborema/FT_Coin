package DAO;

import DTO.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;

public interface OraculoDAO {

    void incluir(OraculoDTO oraculo) throws AppException;

    OraculoDTO consultarPorData(LocalDate data) throws AppException;

    List<OraculoDTO> listarTodas() throws AppException;

    boolean existe(LocalDate data) throws AppException;
}
