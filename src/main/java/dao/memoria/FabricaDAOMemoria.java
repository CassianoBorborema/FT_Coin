package dao.memoria;

import dao.CarteiraDAO;
import dao.FabricaDAO;
import dao.MovimentacaoDAO;
import dao.OraculoDAO;

/**
 * DAOs em memória: os dados se perdem ao sair.
 */
public class FabricaDAOMemoria implements FabricaDAO {

    private final CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
    private final MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOMemoria();
    private final OraculoDAO oraculoDAO = new OraculoDAOMemoria();

    @Override
    public CarteiraDAO carteiraDAO() {
        return carteiraDAO;
    }

    @Override
    public MovimentacaoDAO movimentacaoDAO() {
        return movimentacaoDAO;
    }

    @Override
    public OraculoDAO oraculoDAO() {
        return oraculoDAO;
    }
}
