package DAO.memoria;

import DAO.CarteiraDAO;
import DTO.CarteiraDTO;
import exception.AppException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CarteiraDAOMemoria implements CarteiraDAO {

    private final Map<Integer, CarteiraDTO> carteiras = new HashMap<>();
    private int proximoId = 1;

    @Override
    public void incluir(CarteiraDTO carteira) throws AppException {
        int id = carteira.getIdentificador();

        if (id == 0) {
            id = proximoId++;
        } else if (carteiras.containsKey(id)) {
            throw new AppException("Já existe uma carteira com o identificador " + id + ".");
        } else if (id >= proximoId) {
            proximoId = id + 1;
        }

        CarteiraDTO copia = new CarteiraDTO(id, carteira.getNomeTitular(), carteira.getCorretora());
        carteiras.put(id, copia);
        carteira.setIdentificador(id);
    }

    @Override
    public CarteiraDTO consultarPorId(int identificador) throws AppException {
        CarteiraDTO carteira = carteiras.get(identificador);
        if (carteira == null) {
            throw new AppException("Carteira com identificador " + identificador + " não encontrada.");
        }
        return copiar(carteira);
    }

    @Override
    public List<CarteiraDTO> listarTodas() throws AppException {
        List<CarteiraDTO> lista = new ArrayList<>();
        for (CarteiraDTO carteira : carteiras.values()) {
            lista.add(copiar(carteira));
        }
        return lista;
    }

    @Override
    public void atualizar(CarteiraDTO carteira) throws AppException {
        if (!carteiras.containsKey(carteira.getIdentificador())) {
            throw new AppException("Carteira com identificador " + carteira.getIdentificador() + " não encontrada.");
        }
        carteiras.put(
                carteira.getIdentificador(),
                new CarteiraDTO(carteira.getIdentificador(), carteira.getNomeTitular(), carteira.getCorretora())
        );
    }

    @Override
    public void excluir(int identificador) throws AppException {
        if (carteiras.remove(identificador) == null) {
            throw new AppException("Carteira com identificador " + identificador + " não encontrada.");
        }
    }

    @Override
    public boolean existe(int identificador) throws AppException {
        return carteiras.containsKey(identificador);
    }

    private CarteiraDTO copiar(CarteiraDTO original) {
        return new CarteiraDTO(
                original.getIdentificador(),
                original.getNomeTitular(),
                original.getCorretora()
        );
    }
}
