package controller;

import DAO.OraculoDAO;
import DTO.OraculoDTO;
import exception.AppException;
import model.Oraculo;
import java.time.LocalDate;
import java.util.List;

public class OraculoController {

    private final OraculoDAO oraculoDAO;

    public OraculoController(OraculoDAO oraculoDAO) {
        this.oraculoDAO = oraculoDAO;
    }

    public OraculoDTO cadastrar(LocalDate data, double cotacao) throws AppException {
        Oraculo oraculo = new Oraculo(data, cotacao);
        oraculo.validar();
        OraculoDTO dto = oraculo.toDTO();
        oraculoDAO.incluir(dto);
        return oraculoDAO.consultarPorData(dto.getData());
    }

    public OraculoDTO consultar(LocalDate data) throws AppException {
        if (data == null) {
            throw new AppException("Data da cotação é obrigatória.");
        }
        return oraculoDAO.consultarPorData(data);
    }

    public List<OraculoDTO> listarTodas() throws AppException {
        return oraculoDAO.listarTodas();
    }
}
