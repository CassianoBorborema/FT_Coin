package view;

import app.Main;
import controller.MovimentacaoController;
import DTO.MovimentacaoDTO;
import exception.AppException;
import view.opcoes_menus.OpcoesMenuMovimentacao;
import java.time.LocalDate;
import java.util.Scanner;

public class MenuMovimentacao {

    private final Scanner input;
    private final MovimentacaoController controller;

    public MenuMovimentacao(Scanner input, MovimentacaoController controller) {
        this.input = input;
        this.controller = controller;
    }

    public void exibirMenuMovimentacao() {
        while (true) {
            System.out.println("\nQual opção deseja?");
            System.out.println("1. Compra de moeda virtual");
            System.out.println("2. Venda de moeda virtual");
            System.out.println("0. Voltar");

            OpcoesMenuMovimentacao selecao = OpcoesMenuMovimentacao.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case COMPRA:
                    registrarCompra();
                    break;
                case VENDA:
                    registrarVenda();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }

    private void registrarCompra() {
        try {
            int idCarteira = Main.lerInteiro(input, "Digite o identificador da carteira:");
            LocalDate data = Main.lerData(input, "Digite a data da compra");
            double quantidade = Main.lerDouble(input, "Digite a quantidade:");
            MovimentacaoDTO movimentacao = controller.registrarCompra(idCarteira, data, quantidade);
            System.out.println("Compra registrada com sucesso:");
            System.out.println(movimentacao);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void registrarVenda() {
        try {
            int idCarteira = Main.lerInteiro(input, "Digite o identificador da carteira:");
            LocalDate data = Main.lerData(input, "Digite a data da venda");
            double quantidade = Main.lerDouble(input, "Digite a quantidade:");
            MovimentacaoDTO movimentacao = controller.registrarVenda(idCarteira, data, quantidade);
            System.out.println("Venda registrada com sucesso:");
            System.out.println(movimentacao);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
