package app;

import DAO.CarteiraDAO;
import DAO.memoria.CarteiraDAOMemoria;
import controller.CarteiraController;
import view.MenuPrincipal;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
        CarteiraController carteiraController = new CarteiraController(carteiraDAO);
        MenuPrincipal menu = new MenuPrincipal(input, carteiraController);

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
            int opcao = input.nextInt();
            input.nextLine();
            return opcao;
        } catch (Exception e) {
            input.nextLine();
            System.out.println("Caracter inválido para seleção da opção");
            return -1;
        }
    }

    public static String lerLinha(Scanner input, String mensagem) {
        System.out.println(mensagem);
        return input.nextLine().trim();
    }

    public static int lerInteiro(Scanner input, String mensagem) throws exception.AppException {
        System.out.println(mensagem);
        try {
            int valor = input.nextInt();
            input.nextLine();
            return valor;
        } catch (Exception e) {
            input.nextLine();
            throw new exception.AppException("Valor numérico inválido.");
        }
    }
}
