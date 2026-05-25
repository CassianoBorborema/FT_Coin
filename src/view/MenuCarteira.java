package view;

import app.Main;
import view.opcoes_menus.*;
import java.util.Scanner;

public class MenuCarteira {
    private final Scanner input;

    public MenuCarteira(Scanner input) {
        this.input = input;
    }

    public void exibirMenuCarteira() {
        System.out.println("Qual opção deseja?");
        System.out.println("1. Incluir carteira");
        System.out.println("2. Consultar carteira");
        System.out.println("3. Editar carteira");
        System.out.println("4. Excluir carteira");
        System.out.println("0. Voltar");

        OpcoesMenuCarteira selecao = OpcoesMenuCarteira.opcao(Main.lerOpcao(input));

        while (true) {
            switch (selecao) {
                case INCLUIR:
                    //MenuIncluirCarteira();
                    break;
                case CONSULTAR:
                    break;
                case EDITAR:
                    break;
                case EXCLUIR:
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    throw new RuntimeException("Opção inválida");
            }
        }
    }

    /*
    public static void MenuIncluirCarteira(){
        Carteira carteira = new Carteira();
        Scanner input = new Scanner(System.in);

        //falta exceptions

        System.out.println("Digite o nome do carteira");
        carteira.setId(input.nextInt());

        System.out.println("Digite o nome da corretora");
        carteira.setNome(input.next());

        //falta ID
        //este apenas para teste
        carteira.setId(1);

        System.out.println("Carteira criada");
    }
     */
}
