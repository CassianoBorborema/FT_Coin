package view;

import exception.AppException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base dos menus da aplicação. Exibe as opções numeradas na ordem em que foram adicionadas,
 * lê a escolha, executa a ação correspondente e mostra os erros de negócio, repetindo até o
 * usuário escolher a opção 0.
 */
public abstract class Menu {

    private static final int OPCAO_SAIR = 0;

    protected final Console console;
    private final String titulo;
    private final String rotuloSaida;
    private final List<OpcaoMenu> opcoes = new ArrayList<>();

    /**
     * @param console     entrada e saída da aplicação
     * @param titulo      título exibido acima das opções
     * @param rotuloSaida texto da opção 0
     */
    protected Menu(Console console, String titulo, String rotuloSaida) {
        this.console = console;
        this.titulo = titulo;
        this.rotuloSaida = rotuloSaida;
    }

    /**
     * Cria um menu cuja opção 0 é "Voltar".
     *
     * @param console entrada e saída da aplicação
     * @param titulo  título exibido acima das opções
     */
    protected Menu(Console console, String titulo) {
        this(console, titulo, "Voltar");
    }

    /**
     * Adiciona uma opção, que recebe o próximo número disponível (1, 2, 3...).
     *
     * @param rotulo texto da opção
     * @param acao   ação executada ao escolher a opção
     */
    protected final void adicionarOpcao(String rotulo, OpcaoMenu.Acao acao) {
        opcoes.add(new OpcaoMenu(rotulo, acao));
    }

    /** Exibe o menu até o usuário escolher a opção 0. */
    public void exibir() {
        while (true) {
            exibirOpcoes();
            int escolha = console.lerOpcao();
            if (escolha == OPCAO_SAIR) {
                return;
            }
            if (escolha < 1 || escolha > opcoes.size()) {
                console.exibir("Opção inválida. Tente novamente.");
                continue;
            }
            executar(opcoes.get(escolha - 1));
        }
    }

    private void exibirOpcoes() {
        console.exibir("\n=== " + titulo + " ===");
        for (int i = 0; i < opcoes.size(); i++) {
            console.exibir((i + 1) + ". " + opcoes.get(i).rotulo());
        }
        console.exibir(OPCAO_SAIR + ". " + rotuloSaida);
        console.exibir("Qual opção deseja?");
    }

    private void executar(OpcaoMenu opcao) {
        try {
            opcao.acao().executar();
        } catch (AppException e) {
            console.exibirErro(e.getMessage());
        }
    }
}
