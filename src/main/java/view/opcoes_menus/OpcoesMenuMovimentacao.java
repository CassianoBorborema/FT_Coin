package view.opcoes_menus;

public enum OpcoesMenuMovimentacao {
    COMPRA(1), VENDA(2), VOLTAR(0), INVALIDA(-1);

    private int valor;

    OpcoesMenuMovimentacao(int valor) {
        this.valor = valor;
    }

    public static OpcoesMenuMovimentacao opcao(int i) {
        for (OpcoesMenuMovimentacao h : values()) {
            if (h.valor == i) return h;
        }
        return INVALIDA;
    }
}
