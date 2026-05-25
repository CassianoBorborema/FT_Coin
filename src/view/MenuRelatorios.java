package view;

import app.Main;
import view.opcoes_menus.*;
import java.util.Scanner;

public class MenuRelatorios {
    private final Scanner input;

    public MenuRelatorios(Scanner input) {
        this.input = input;
    }

    public void exibirMenuRelatorios() {
        while (true) {
            System.out.println("Qual opção deseja?");
            System.out.println("1. Buscar por identificador");
            System.out.println("2. Buscar por titular");
            System.out.println("3. Saldo atual");
            System.out.println("4. Histórico de movimentação");
            System.out.println("5. Ganhos e perdas");
            System.out.println("0. Voltar");

            OpcoesMenuRelatorios selecao = OpcoesMenuRelatorios.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case LISTAR_POR_ID:
                    System.out.println("Listando carteiras por identificador...");
                    break;
                case LISTAR_POR_NOME:
                    System.out.println("Listando carteiras por nome do titular...");
                    break;
                case EXIBIR_SALDO:
                    System.out.println("Digite o identificador da carteira");
                    break;
                case HISTORICO_MOVIMENTACAO:
                    System.out.println("Digite o identificador da carteira");
                    break;
                case GANHO_PERDA:
                    System.out.println("Apresentando ganho ou perda total de cada carteira...");
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    throw new RuntimeException("Opção inválida");
            }
        }
    }
}
