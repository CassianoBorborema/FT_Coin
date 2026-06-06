package view;

import app.Main;
import control.CarteiraController;
import DTO.CarteiraDTO;
import exception.AppException;
import view.opcoes_menus.OpcoesMenuCarteira;
import java.util.Scanner;

public class MenuCarteira {

    private final Scanner input;
    private final CarteiraController controller;

    public MenuCarteira(Scanner input, CarteiraController controller) {
        this.input = input;
        this.controller = controller;
    }

    public void exibirMenuCarteira() {
        while (true) {
            System.out.println("\nQual opção deseja?");
            System.out.println("1. Incluir carteira");
            System.out.println("2. Consultar carteira");
            System.out.println("3. Editar carteira");
            System.out.println("4. Excluir carteira");
            System.out.println("0. Voltar");

            OpcoesMenuCarteira selecao = OpcoesMenuCarteira.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case INCLUIR:
                    incluirCarteira();
                    break;
                case CONSULTAR:
                    consultarCarteira();
                    break;
                case EDITAR:
                    editarCarteira();
                    break;
                case EXCLUIR:
                    excluirCarteira();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }

    private void incluirCarteira() {
        try {
            String nomeTitular = Main.lerLinha(input, "Digite o nome do titular:");
            String corretora = Main.lerLinha(input, "Digite a corretora:");
            CarteiraDTO carteira = controller.incluir(nomeTitular, corretora);
            System.out.println("Carteira incluída com sucesso:");
            System.out.println(carteira);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void consultarCarteira() {
        try {
            int identificador = Main.lerInteiro(input, "Digite o identificador da carteira:");
            CarteiraDTO carteira = controller.consultar(identificador);
            System.out.println("Carteira encontrada:");
            System.out.println(carteira);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void editarCarteira() {
        try {
            int identificador = Main.lerInteiro(input, "Digite o identificador da carteira a editar:");
            CarteiraDTO atual = controller.consultar(identificador);
            System.out.println("Carteira atual: " + atual);

            String nomeTitular = Main.lerLinha(input, "Digite o novo nome do titular:");
            String corretora = Main.lerLinha(input, "Digite a nova corretora:");
            CarteiraDTO atualizada = controller.editar(identificador, nomeTitular, corretora);
            System.out.println("Carteira atualizada com sucesso:");
            System.out.println(atualizada);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void excluirCarteira() {
        try {
            int identificador = Main.lerInteiro(input, "Digite o identificador da carteira a excluir:");
            CarteiraDTO carteira = controller.consultar(identificador);
            System.out.println("Carteira a excluir: " + carteira);
            String confirmacao = Main.lerLinha(input, "Confirma exclusão? (S/N):");

            if (confirmacao.equalsIgnoreCase("S")) {
                controller.excluir(identificador);
                System.out.println("Carteira excluída com sucesso.");
            } else {
                System.out.println("Exclusão cancelada.");
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
