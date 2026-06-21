package view;

import app.Main;
import controller.CarteiraController;
import controller.MovimentacaoController;
import controller.OraculoController;
import controller.RelatorioController;
import view.opcoes_menus.OpcoesMenuPrincipal;
import java.util.Scanner;
import view.opcoes_menus.OpcoesMenuPrincipal;

public class MenuPrincipal {

    private final Scanner input;
    private final CarteiraController carteiraController;
    private final MovimentacaoController movimentacaoController;
    private final OraculoController oraculoController;
    private final RelatorioController relatorioController;

    public MenuPrincipal(Scanner input, CarteiraController carteiraController,
                         MovimentacaoController movimentacaoController,
                         OraculoController oraculoController,
                         RelatorioController relatorioController) {
        this.input = input;
        this.carteiraController = carteiraController;
        this.movimentacaoController = movimentacaoController;
        this.oraculoController = oraculoController;
        this.relatorioController = relatorioController;
    }

    public void exibirMenuPrincipal() {
        while (true) {
            System.out.println("\nQual opcao deseja selecionar? (Digite o respectivo numero)");
            System.out.println("1. Carteira");
            System.out.println("2. Movimentação");
            System.out.println("3. Oráculo");
            System.out.println("4. Relatórios");
            System.out.println("5. Ajuda");
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
                case ORACULO:
                    MenuOraculo menuOraculo = new MenuOraculo(input, oraculoController);
                    menuOraculo.exibirMenuOraculo();
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
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }
}