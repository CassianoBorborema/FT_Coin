package view;

/**
 * Menu raiz da aplicação, que leva aos menus de cada área.
 */
public class MenuPrincipal extends Menu {

    public MenuPrincipal(Console console, Menu carteira, Menu movimentacao, Menu oraculo,
                         Menu relatorios, Menu ajuda) {
        super(console, "FT_Coin - Menu principal", "Sair");
        adicionarOpcao("Carteira", carteira::exibir);
        adicionarOpcao("Movimentação", movimentacao::exibir);
        adicionarOpcao("Oráculo", oraculo::exibir);
        adicionarOpcao("Relatórios", relatorios::exibir);
        adicionarOpcao("Ajuda", ajuda::exibir);
    }
}
