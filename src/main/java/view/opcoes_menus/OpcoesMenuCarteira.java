package view.opcoes_menus;

public enum OpcoesMenuCarteira {
    INCLUIR(1), CONSULTAR(2), EDITAR(3), EXCLUIR(4), VOLTAR(0), INVALIDA(-1);

    private int valor;

    OpcoesMenuCarteira(int valor) {
        this.valor = valor;
    }

    public static OpcoesMenuCarteira opcao(int i) {
        for (OpcoesMenuCarteira h : values()) {
            if (h.valor == i) return h;
        }
        return INVALIDA;
    }
}
