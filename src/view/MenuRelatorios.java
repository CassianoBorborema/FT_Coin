package view;

import DTO.CarteiraDTO;
import DTO.MovimentacaoDTO;
import app.Main;
import controller.RelatorioController;
import DTO.CarteiraDTO;
import DTO.MovimentacaoDTO;
import exception.AppException;
import view.opcoes_menus.OpcoesMenuRelatorios;
import java.util.List;
import java.util.Scanner;
import view.opcoes_menus.OpcoesMenuRelatorios;

public class MenuRelatorios {

    private final Scanner input;
    private final RelatorioController controller;

    public MenuRelatorios(Scanner input, RelatorioController controller) {
        this.input = input;
        this.controller = controller;
    }

    public void exibirMenuRelatorios() {
        while (true) {
            System.out.println("\nQual opção deseja?");
            System.out.println("1. Buscar por identificador");
            System.out.println("2. Buscar por titular");
            System.out.println("3. Saldo atual");
            System.out.println("4. Histórico de movimentação");
            System.out.println("5. Ganhos e perdas");
            System.out.println("0. Voltar");

            OpcoesMenuRelatorios selecao = OpcoesMenuRelatorios.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case LISTAR_POR_ID:
                    listarPorIdentificador();
                    break;
                case LISTAR_POR_NOME:
                    listarPorTitular();
                    break;
                case EXIBIR_SALDO:
                    exibirSaldo();
                    break;
                case HISTORICO_MOVIMENTACAO:
                    exibirHistorico();
                    break;
                case GANHO_PERDA:
                    exibirGanhoPerda();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }

    private void listarPorIdentificador() {
        try {
            List<CarteiraDTO> carteiras = controller.listarPorIdentificador();
            exibirCarteiras(carteiras, "identificador");
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void listarPorTitular() {
        try {
            List<CarteiraDTO> carteiras = controller.listarPorTitular();
            exibirCarteiras(carteiras, "titular");
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirSaldo() {
        try {
            int identificador = Main.lerInteiro(input, "Digite o identificador da carteira:");
            double saldo = controller.calcularSaldo(identificador);
            System.out.println("Saldo atual da carteira " + identificador + ": " + saldo);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirHistorico() {
        try {
            int identificador = Main.lerInteiro(input, "Digite o identificador da carteira:");
            List<MovimentacaoDTO> movimentacoes = controller.historico(identificador);
            if (movimentacoes.isEmpty()) {
                System.out.println("Nenhuma movimentação registrada para a carteira " + identificador + ".");
                return;
            }
            System.out.println("Histórico da carteira " + identificador + ":");
            for (MovimentacaoDTO movimentacao : movimentacoes) {
                System.out.println(movimentacao);
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirGanhoPerda() {
        try {
            List<CarteiraDTO> carteiras = controller.listarPorIdentificador();
            if (carteiras.isEmpty()) {
                System.out.println("Nenhuma carteira cadastrada.");
                return;
            }
            System.out.println("Ganho/perda por carteira:");
            for (CarteiraDTO carteira : carteiras) {
                int id = carteira.getIdentificador();
                try {
                    double resultado = controller.ganhoPerda(id);
                    System.out.println("Carteira " + id + " (" + carteira.getNomeTitular() + "): " + resultado);
                } catch (AppException e) {
                    System.out.println("Carteira " + id + " (" + carteira.getNomeTitular() + "): " + e.getMessage());
                }
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirCarteiras(List<CarteiraDTO> carteiras, String criterio) {
        if (carteiras.isEmpty()) {
            System.out.println("Nenhuma carteira cadastrada.");
            return;
        }
        System.out.println("Carteiras ordenadas por " + criterio + ":");
        for (CarteiraDTO carteira : carteiras) {
            System.out.println(carteira);
        }
    }
}
