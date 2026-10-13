package dao;

/**
 * Cria os DAOs de uma forma de persistência (padrão Abstract Factory). Trocar a fábrica troca
 * a persistência de toda a aplicação sem alterar controllers e views.
 */
public interface FabricaDAO {

    CarteiraDAO carteiraDAO();

    MovimentacaoDAO movimentacaoDAO();

    OraculoDAO oraculoDAO();
}
