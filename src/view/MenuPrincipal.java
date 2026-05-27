package view;

import app.Main;
import view.opcoes_menus.*;
import java.util.Scanner;

public class MenuPrincipal {
    private final Scanner input;

    public MenuPrincipal(Scanner input) {
        this.input = input;
    }

    public void exibirMenuPrincipal() {
        while (true) {
            System.out.println("\nQual opção deseja selecionar? (Digite o respectivo número)");
            System.out.println("1. Carteira");
            System.out.println("2. Movimentação");
            System.out.println("3. Relatórios");
            System.out.println("4. Ajuda");
            System.out.println("0. Sair");

            OpcoesMenuPrincipal selecao = OpcoesMenuPrincipal.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case CARTEIRA:
                    MenuCarteira menuCarteira = new MenuCarteira(input);
                    menuCarteira.exibirMenuCarteira();
                    break;
                case MOVIMENTACAO:
                    break;
                case RELATORIOS:
                    break;
                case AJUDA:
                    break;
                case SAIR:
                    System.out.println("Saindo da aplicação...");
                    input.close();
                    System.exit(0);
                case INVALIDA:
                    throw new RuntimeException("Erro: Opção inválida selecionada");
            }
        }
    }
}
