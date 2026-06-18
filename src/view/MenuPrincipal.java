package view;

import app.Main;
import controller.CarteiraController;
import controller.MovimentacaoController;
import controller.RelatorioController;
import java.util.Scanner;
import view.opcoes_menus.OpcoesMenuPrincipal;

public class MenuPrincipal {

    private final Scanner input;
    private final CarteiraController carteiraController;
    private final MovimentacaoController movimentacaoController;
    private final RelatorioController relatorioController;

    public MenuPrincipal(Scanner input,
                         CarteiraController carteiraController,
                         MovimentacaoController movimentacaoController,
                         RelatorioController relatorioController) {
        this.input                  = input;
        this.carteiraController     = carteiraController;
        this.movimentacaoController = movimentacaoController;
        this.relatorioController    = relatorioController;
    }

    public void exibirMenuPrincipal() {
        while (true) {
            System.out.println("\nQual opcao deseja selecionar? (Digite o respectivo numero)");
            System.out.println("1. Carteira");
            System.out.println("2. Movimentacao");
            System.out.println("3. Relatorios");
            System.out.println("4. Ajuda");
            System.out.println("0. Sair");

            OpcoesMenuPrincipal selecao = OpcoesMenuPrincipal.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case CARTEIRA:
                    MenuCarteira menuCarteira = new MenuCarteira(input, carteiraController);
                    menuCarteira.exibirMenuCarteira();
                    break;
                case MOVIMENTACAO:
                    MenuMovimentacao menuMovimentacao = new MenuMovimentacao(input, movimentacaoController);
                    menuMovimentacao.exibirMenuMovimentacao();
                    break;
                case RELATORIOS:
                    MenuRelatorios menuRelatorios = new MenuRelatorios(input, relatorioController);
                    menuRelatorios.exibirMenuRelatorios();
                    break;
                case AJUDA:
                    MenuAjuda menuAjuda = new MenuAjuda(input);
                    menuAjuda.exibirMenuAjuda();
                    break;
                case SAIR:
                    System.out.println("Saindo da aplicacao...");
                    input.close();
                    System.exit(0);
                    break;
                case INVALIDA:
                    System.out.println("Opcao invalida. Tente novamente.");
                    break;
            }
        }
    }
}