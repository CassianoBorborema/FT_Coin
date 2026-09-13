package view;

import app.Main;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import view.opcoes_menus.OpcoesMenuAjuda;

public class MenuAjuda {

    private static final String CAMINHO_TEXTO_AJUDA = "/textos/ajuda.txt";
    private static final String CAMINHO_CREDITOS = "/textos/creditos.txt";

    private final Scanner input;

    public MenuAjuda(Scanner input) {
        this.input = input;
    }

    public void exibirMenuAjuda() {
        while (true) {
            System.out.println("\nQual opcao deseja?");
            System.out.println("1. Como funciona");
            System.out.println("2. Créditos");
            System.out.println("0. Voltar");

            OpcoesMenuAjuda selecao = OpcoesMenuAjuda.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case TEXTO_AJUDA:
                    exibirTextoAjuda();
                    break;
                case CREDITOS:
                	exibirCreditos();
                	break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }

    // Secção de ajuda
    private void exibirTextoAjuda() {
        System.out.println();
        System.out.println(lerRecurso(CAMINHO_TEXTO_AJUDA));
    }

    // Secção de créditos
    private void exibirCreditos() {
        System.out.println();
        System.out.println(lerRecurso(CAMINHO_CREDITOS));
    }

    private String lerRecurso(String caminho) {
        try (InputStream in = getClass().getResourceAsStream(caminho)) {
            if (in == null) {
                return "Texto indisponível (arquivo não encontrado: " + caminho + ").";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Texto indisponível (erro ao ler: " + caminho + ").";
        }
    }
}