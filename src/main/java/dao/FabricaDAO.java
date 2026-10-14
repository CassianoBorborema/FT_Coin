package dao;

/**
 * Cria os DAOs de uma forma de persistência (Abstract Factory).
 */
public interface FabricaDAO {

    CarteiraDAO carteiraDAO();

    MovimentacaoDAO movimentacaoDAO();

    OraculoDAO oraculoDAO();
}
