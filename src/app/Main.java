package app;
import view.*;
import view.opcoes_menus.*;
import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        MenuPrincipal menu = new MenuPrincipal(input);
        System.out.println("****************************");
        System.out.println("====Bem vindo ao FT_Coin====");
        System.out.println("****************************");

        try {
            menu.exibirMenuPrincipal();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static int lerOpcao(Scanner input) {
        try {
            // Tenta ler o número
            int opcao = input.nextInt();

            // Limpar buffer
            input.nextLine();

            return opcao;
        } catch (Exception e) {
            // Limpar buffer
            input.nextLine();
            System.out.println("Caracter inválido para seleção da opção");
            return -1;
        }
    }
}