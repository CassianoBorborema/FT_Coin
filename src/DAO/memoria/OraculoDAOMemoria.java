package DAO.memoria;

import DAO.OraculoDAO;
import DTO.OraculoDTO;
import exception.AppException;
import model.Oraculo;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
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
    public List<OraculoDTO> listarTodas() throws AppException {
        List<OraculoDTO> lista = new ArrayList<>();
        for (OraculoDTO cotacao : cotacoes.values()) {
            lista.add(new OraculoDTO(cotacao.getData(), cotacao.getCotacao()));
        }
        lista.sort(Comparator.comparing(OraculoDTO::getData));
        return lista;
    }

    @Override
    public boolean existe(LocalDate data) throws AppException {
        return cotacoes.containsKey(data);
    }
}
