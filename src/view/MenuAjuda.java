package view;

import app.Main;
import view.opcoes_menus.*;
import java.util.Scanner;

public class MenuAjuda {
    private final Scanner input;

    public MenuAjuda(Scanner input) {
        this.input = input;
    }

    public void exibirMenuAjuda() {
        while (true) {
            System.out.println("Qual opção deseja?");
            System.out.println("1. Como funciona");
            System.out.println("2. Créditos");
            System.out.println("0. Voltar");

            OpcoesMenuAjuda selecao = OpcoesMenuAjuda.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case TEXTO_AJUDA:
                    //menu ajuda
                    break;
                case CREDITOS:
                    exibirCreditos();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    throw new RuntimeException("Opção inválida");
            }
        }
    }

    private void exibirCreditos() {
        System.out.println("=== CRÉDITOS ===");
        System.out.println("Autores: ");
        System.out.println("Data: ");
    }
}
