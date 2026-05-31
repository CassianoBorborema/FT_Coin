package DAO;

import DTO.CarteiraDTO;
import exception.AppException;
import java.util.List;

public interface CarteiraDAO {

    void incluir(CarteiraDTO carteira) throws AppException;

    CarteiraDTO consultarPorId(int identificador) throws AppException;

    List<CarteiraDTO> listarTodas() throws AppException;

    void atualizar(CarteiraDTO carteira) throws AppException;

    void excluir(int identificador) throws AppException;

    boolean existe(int identificador) throws AppException;
}
