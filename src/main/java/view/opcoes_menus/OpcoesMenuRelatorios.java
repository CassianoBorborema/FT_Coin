package view.opcoes_menus;

public enum OpcoesMenuRelatorios {
    LISTAR_POR_ID(1), LISTAR_POR_NOME(2), EXIBIR_SALDO(3), HISTORICO_MOVIMENTACAO(4), GANHO_PERDA(5), VOLTAR(0), INVALIDA(-1);

    private int valor;

    OpcoesMenuRelatorios(int valor) {
        this.valor = valor;
    }

    public static OpcoesMenuRelatorios opcao(int i) {
        for (OpcoesMenuRelatorios h : values()) {
            if (h.valor == i) return h;
        }
        return INVALIDA;
    }
}
