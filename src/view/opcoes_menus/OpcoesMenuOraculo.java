package view.opcoes_menus;

public enum OpcoesMenuOraculo {
    CADASTRAR(1), CONSULTAR(2), LISTAR(3), VOLTAR(0), INVALIDA(-1);

    private int valor;

    OpcoesMenuOraculo(int valor) {
        this.valor = valor;
    }

    public static OpcoesMenuOraculo opcao(int i) {
        for (OpcoesMenuOraculo h : values()) {
            if (h.valor == i) return h;
        }
        return INVALIDA;
    }
}
