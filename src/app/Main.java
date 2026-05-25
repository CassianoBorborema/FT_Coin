package app;
import view.*;
import view.opcoes_menus.*;
import java.util.Scanner;

public class Main{
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        MenuPrincipal menu = new MenuPrincipal(input);
        System.out.println("Bem vindo ao FT coin");
        menu.exibirMenuPrincipal();
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
            return -1;
        }
    }
}