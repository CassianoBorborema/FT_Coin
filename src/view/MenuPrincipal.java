package view;

import app.Main;
import control.CarteiraController;
import control.MovimentacaoController;
import control.RelatorioController;
import view.opcoes_menus.OpcoesMenuPrincipal;
import java.util.Scanner;

public class MenuPrincipal {

    private final Scanner input;
    private final CarteiraController carteiraController;
    private final MovimentacaoController movimentacaoController;
    private final RelatorioController relatorioController;


    public MenuPrincipal(Scanner input, CarteiraController carteiraController, MovimentacaoController movimentacaoController, RelatorioController relatorioController) {
		this.input = input;
		this.carteiraController = carteiraController;
		this.movimentacaoController = movimentacaoController;
		this.relatorioController = relatorioController;
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
                    System.out.println("Saindo da aplicação...");
                    input.close();
                    System.exit(0);
                case INVALIDA:
                    throw new RuntimeException("Erro: Opção inválida selecionada");
            }
        }
    }
}
