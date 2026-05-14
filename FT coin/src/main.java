import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        System.out.println("Bem vindo ao FT coin");
        while (true) {
            System.out.println("Qual opção deseja?");
            System.out.println("1. Carteira");
            System.out.println("2. Movimentação");
            System.out.println("3. Relatórios");
            System.out.println("4. Ajuda");
            System.out.println("0. Sair");

            int opcao = input.nextInt();

            switch (opcao) {
                case 1:
                    MenuCarteira();
                case 2:
                    System.out.println("Digite o nome do carteira");
                    continue;
                case 3:
                    System.out.println("Digite o nome do carteira");
                    continue;
                case 4:
                    System.out.println("Digite o nome do carteira");
                    continue;
                case 0:
                    System.exit(0);
                default:
                    System.out.println("Opção inválida");
                    break;
            }
        }
    }

    public static void MenuCarteira() {
        Scanner input = new Scanner(System.in);
        int opcao = input.nextInt();

        System.out.println("Qual opção deseja?");
        System.out.println("1. Incluir carteira");
        System.out.println("2. Consultar carteira");
        System.out.println("3. Editar carteira");
        System.out.println("4. Excluir carteira");
        System.out.println("4. Voltar");

        while (true) {
            switch (opcao) {
                case 1:
                    MenuIncluirCarteira();
                case 2:
                    System.out.println("Digite o nome do carteira");

                case 3:
                    System.out.println("Digite o nome do carteira");

                case 4:
                    System.out.println("Digite o nome do carteira");
                case 0:
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    public static void MenuIncluirCarteira(){
        Carteira carteira = new Carteira();
        Scanner input = new Scanner(System.in);

        //falta exceptions

        System.out.println("Digite o as informações a seguir:");

        System.out.println("Digite o nome do carteira");
        carteira.setId(input.nextInt());

        System.out.println("Digite o nome da corretora");
        carteira.setNome(input.next());

        //falta ID


        System.out.println("Carteira criada");

        return;
    }

    public static void MenuConsultarCarteira(){}
    public static void MenuEditarCarteira(){}
    public static void MenuExcluirCarteira(){}
}
