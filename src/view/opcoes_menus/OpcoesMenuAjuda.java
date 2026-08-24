package view.opcoes_menus;

public enum OpcoesMenuAjuda {
    TEXTO_AJUDA(1), VOLTAR(0), INVALIDA(-1);

    private int valor;

    OpcoesMenuAjuda(int valor) {
        this.valor = valor;
    }

    public static OpcoesMenuAjuda opcao(int i) {
        for (OpcoesMenuAjuda h : values()) {
            if (h.valor == i) return h;
        }
        return INVALIDA;
    }
}
