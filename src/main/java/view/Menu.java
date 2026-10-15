package view;

import config.Mensagens;
import dto.CarteiraDTO;
import exception.AppException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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
        this(console, titulo, Mensagens.get("menu.voltar"));
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
                console.exibir(Mensagens.get("menu.opcaoInvalida"));
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
        console.exibir(Mensagens.get("menu.pergunta"));
    }

    /**
     * Lista as carteiras por titular e pede que o usuário escolha uma pelo número da lista, para que
     * ele não precise saber o identificador. Repete até receber um número válido.
     *
     * @return a carteira escolhida, ou vazio se o usuário cancelar com 0
     * @throws AppException se não houver carteira cadastrada
     */
    protected final Optional<CarteiraDTO> escolherCarteira(List<CarteiraDTO> carteiras) throws AppException {
        if (carteiras.isEmpty()) {
            throw new AppException("erro.carteira.nenhuma");
        }
        List<CarteiraDTO> ordenadas = carteiras.stream()
                .sorted(Comparator.comparing(CarteiraDTO::getNomeTitular, String.CASE_INSENSITIVE_ORDER)
                        .thenComparingInt(CarteiraDTO::getIdentificador))
                .toList();
        while (true) {
            console.exibir(Mensagens.get("carteira.escolher"));
            for (int i = 0; i < ordenadas.size(); i++) {
                console.exibir("  " + (i + 1) + ". " + Formatador.carteira(ordenadas.get(i)));
            }
            console.exibir("  " + OPCAO_SAIR + ". " + Mensagens.get("menu.cancelar"));
            int escolha = console.lerOpcao();
            if (escolha == OPCAO_SAIR) {
                return Optional.empty();
            }
            if (escolha >= 1 && escolha <= ordenadas.size()) {
                return Optional.of(ordenadas.get(escolha - 1));
            }
            console.exibir(Mensagens.get("menu.opcaoInvalida"));
        }
    }

    /** Nenhum erro de uma opção encerra a aplicação: o usuário vê a mensagem e volta ao menu. */
    private void executar(Opcao opcao) {
        try {
            opcao.acao().executar();
        } catch (AppException e) {
            console.exibirErro(e.getMessage());
        } catch (RuntimeException e) {
            console.exibirErro(Mensagens.get("menu.erroInesperado", e.getClass().getSimpleName(), e.getMessage()));
        }
    }
}
