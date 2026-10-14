package dao.mariadb;

import dao.CarteiraDAO;
import dao.FabricaDAO;
import dao.MovimentacaoDAO;
import dao.OraculoDAO;
import infra.db.FabricaConexao;

/**
 * DAOs que gravam no MariaDB.
 */
public class FabricaDAOMariaDB implements FabricaDAO {

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final OraculoDAO oraculoDAO;

    public FabricaDAOMariaDB(FabricaConexao conexoes) {
        this.carteiraDAO = new CarteiraDAOMariaDB(conexoes);
        this.movimentacaoDAO = new MovimentacaoDAOMariaDB(conexoes);
        this.oraculoDAO = new OraculoDAOMariaDB(conexoes);
    }

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
