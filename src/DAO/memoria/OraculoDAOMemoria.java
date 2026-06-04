package DAO.memoria;

import DAO.OraculoDAO;
import DTO.OraculoDTO;
import exception.AppException;
import model.Oraculo;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class OraculoDAOMemoria implements OraculoDAO {

    private final Map<LocalDate, OraculoDTO> cotacoes = new HashMap<>();

    @Override
    public void incluir(OraculoDTO oraculo) throws AppException {
        Oraculo entidade = Oraculo.fromDTO(oraculo);
        entidade.validar();
        OraculoDTO copia = entidade.toDTO();
        if (cotacoes.containsKey(copia.getData())) {
            throw new AppException("Já existe cotação cadastrada para a data " + copia.getData() + ".");
        }
        cotacoes.put(copia.getData(), copia);
    }

    @Override
    public OraculoDTO consultarPorData(LocalDate data) throws AppException {
        OraculoDTO oraculo = cotacoes.get(data);
        if (oraculo == null) {
            throw new AppException("Não há cotação cadastrada para a data " + data + ".");
        }
        return new OraculoDTO(oraculo.getData(), oraculo.getCotacao());
    }

    @Override
    public boolean existe(LocalDate data) throws AppException {
        return cotacoes.containsKey(data);
    }
}
