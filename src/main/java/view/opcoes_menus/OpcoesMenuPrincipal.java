package view.opcoes_menus;

public enum OpcoesMenuPrincipal {
    CARTEIRA(1), MOVIMENTACAO(2), ORACULO(3), RELATORIOS(4), AJUDA(5), SAIR(0), INVALIDA(-1);

    private int valor;

    OpcoesMenuPrincipal(int valor) {
        this.valor = valor;
    }

    public static OpcoesMenuPrincipal opcao(int i) {
        for (OpcoesMenuPrincipal h : values()) {
            if (h.valor == i) return h;
        }
        return INVALIDA;
    }
}
