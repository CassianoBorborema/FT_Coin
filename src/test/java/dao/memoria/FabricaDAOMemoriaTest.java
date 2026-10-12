package dao.memoria;

import dao.FabricaDAO;
import dao.FabricaDAOContrato;

class FabricaDAOMemoriaTest extends FabricaDAOContrato {

    @Override
    protected FabricaDAO criarFabricaVazia() {
        return new FabricaDAOMemoria();
    }
}
