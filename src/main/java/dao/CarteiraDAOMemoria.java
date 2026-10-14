package dao;

import dto.CarteiraDTO;
import exception.AppException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Carteiras em memória; os dados se perdem ao fechar a aplicação.
 */
final class CarteiraDAOMemoria implements CarteiraDAO {

    private final Map<Integer, CarteiraDTO> carteiras = new HashMap<>();

    @Override
    public void incluir(CarteiraDTO carteira) throws AppException {
        int id = carteira.getIdentificador();

        if (id == 0) {
            id = menorIdentificadorLivre();
        } else if (carteiras.containsKey(id)) {
            throw new AppException("erro.carteira.duplicada", id);
        }

        CarteiraDTO copia = new CarteiraDTO(id, carteira.getNomeTitular(), carteira.getCorretora());
        carteiras.put(id, copia);
        carteira.setIdentificador(id);
    }

    @Override
    public CarteiraDTO consultarPorId(int identificador) throws AppException {
        CarteiraDTO carteira = carteiras.get(identificador);
        if (carteira == null) {
            throw new AppException("erro.carteira.naoEncontrada", identificador);
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
            throw new AppException("erro.carteira.naoEncontrada", carteira.getIdentificador());
        }
        carteiras.put(
                carteira.getIdentificador(),
                new CarteiraDTO(carteira.getIdentificador(), carteira.getNomeTitular(), carteira.getCorretora())
        );
    }

    @Override
    public void excluir(int identificador) throws AppException {
        if (carteiras.remove(identificador) == null) {
            throw new AppException("erro.carteira.naoEncontrada", identificador);
        }
    }

    @Override
    public boolean existe(int identificador) throws AppException {
        return carteiras.containsKey(identificador);
    }

    /**
     * Procura o menor identificador positivo ainda não utilizado,
     * reaproveitando os que foram liberados por exclusões.
     *
     * @return o menor identificador livre
     */
    private int menorIdentificadorLivre() {
        int id = 1;
        while (carteiras.containsKey(id)) {
            id++;
        }
        return id;
    }

    private CarteiraDTO copiar(CarteiraDTO original) {
        return new CarteiraDTO(
                original.getIdentificador(),
                original.getNomeTitular(),
                original.getCorretora()
        );
    }
}
