package view;

import config.Mensagens;

/**
 * Menu raiz da aplicação, que leva aos menus de cada área.
 */
public class MenuPrincipal extends Menu {

    public MenuPrincipal(Console console, Menu carteira, Menu movimentacao, Menu oraculo,
                         Menu relatorios, Menu ajuda) {
        super(console, Mensagens.get("principal.titulo"), Mensagens.get("principal.sair"));
        adicionarOpcao(Mensagens.get("carteira.titulo"), carteira::exibir);
        adicionarOpcao(Mensagens.get("movimentacao.titulo"), movimentacao::exibir);
        adicionarOpcao(Mensagens.get("oraculo.titulo"), oraculo::exibir);
        adicionarOpcao(Mensagens.get("relatorios.titulo"), relatorios::exibir);
        adicionarOpcao(Mensagens.get("ajuda.titulo"), ajuda::exibir);
    }
}
