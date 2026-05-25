package view;

import app.Main;
import view.opcoes_menus.*;
import java.util.Scanner;

public class MenuMovimentacao {
    private final Scanner input;

    public MenuMovimentacao(Scanner input) {
        this.input = input;
    }

    public void exibirMenuMovimentacao() {
        while (true) {
            System.out.println("Qual opção deseja?");
            System.out.println("1. Compra de moeda virtual");
            System.out.println("2. Venda de moeda virtual");
            System.out.println("0. Voltar");

            OpcoesMenuMovimentacao selecao = OpcoesMenuMovimentacao.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case COMPRA:
                    //menu
                    break;
                case VENDA:
                    //menu
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    throw new RuntimeException("Opção inválida");
            }
        }
    }


}
