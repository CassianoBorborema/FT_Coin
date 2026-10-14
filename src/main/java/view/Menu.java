package view;

import exception.AppException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base dos menus: numera as opções na ordem em que foram adicionadas, executa a escolhida e
 * exibe os erros de negócio, até o usuário escolher 0.
 */
public abstract class Menu {

    private static final int OPCAO_SAIR = 0;

    protected final Console console;
    private final String titulo;
    private final String rotuloSaida;
    private final List<Opcao> opcoes = new ArrayList<>();

    @FunctionalInterface
    public interface Acao {
        void executar() throws AppException;
    }

    private record Opcao(String rotulo, Acao acao) {
    }

    protected Menu(Console console, String titulo, String rotuloSaida) {
        this.console = console;
        this.titulo = titulo;
        this.rotuloSaida = rotuloSaida;
    }

    protected Menu(Console console, String titulo) {
        this(console, titulo, "Voltar");
    }

    protected final void adicionarOpcao(String rotulo, Acao acao) {
        opcoes.add(new Opcao(rotulo, acao));
    }

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

    /** Nenhum erro de uma opção encerra a aplicação: o usuário vê a mensagem e volta ao menu. */
    private void executar(Opcao opcao) {
        try {
            opcao.acao().executar();
        } catch (AppException e) {
            console.exibirErro(e.getMessage());
        } catch (RuntimeException e) {
            console.exibirErro("Erro inesperado (" + e.getClass().getSimpleName() + "): " + e.getMessage());
        }
    }
}
