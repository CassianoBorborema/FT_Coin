package app;

import DAO.CarteiraDAO;
import DAO.MovimentacaoDAO;
import DAO.OraculoDAO;
import DAO.memoria.CarteiraDAOMemoria;
import DAO.memoria.MovimentacaoDAOMemoria;
import DAO.memoria.OraculoDAOMemoria;
import DTO.OraculoDTO;
import controller.CarteiraController;
import controller.MovimentacaoController;
import exception.AppException;
import view.MenuPrincipal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Main {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
        OraculoDAO oraculoDAO = new OraculoDAOMemoria();
        MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOMemoria();

        seedOraculo(oraculoDAO);

        CarteiraController carteiraController = new CarteiraController(carteiraDAO, movimentacaoDAO);
        MovimentacaoController movimentacaoController = new MovimentacaoController(
                movimentacaoDAO, carteiraDAO, oraculoDAO
        );
        MenuPrincipal menu = new MenuPrincipal(input, carteiraController, movimentacaoController);

        System.out.println("****************************");
        System.out.println("====Bem vindo ao FT_Coin====");
        System.out.println("****************************");

        try {
            menu.exibirMenuPrincipal();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private static void seedOraculo(OraculoDAO oraculoDAO) {
        try {
            LocalDate hoje = LocalDate.now();
            oraculoDAO.incluir(new OraculoDTO(hoje, 150.0));
            oraculoDAO.incluir(new OraculoDTO(hoje.minusDays(1), 145.0));
        } catch (AppException e) {
            System.out.println("Aviso ao carregar cotações iniciais: " + e.getMessage());
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

    public static int lerInteiro(Scanner input, String mensagem) throws AppException {
        System.out.println(mensagem);
        try {
            int valor = input.nextInt();
            input.nextLine();
            return valor;
        } catch (Exception e) {
            input.nextLine();
            throw new AppException("Valor numérico inválido.");
        }
    }

    public static double lerDouble(Scanner input, String mensagem) throws AppException {
        System.out.println(mensagem);
        try {
            double valor = input.nextDouble();
            input.nextLine();
            return valor;
        } catch (Exception e) {
            input.nextLine();
            throw new AppException("Valor decimal inválido.");
        }
    }

    public static LocalDate lerData(Scanner input, String mensagem) throws AppException {
        String texto = lerLinha(input, mensagem + " (dd/MM/yyyy):");
        try {
            return LocalDate.parse(texto, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new AppException("Data inválida. Use o formato dd/MM/yyyy.");
        }
    }
}
