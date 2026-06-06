package view;

import app.Main;
import control.RelatorioController;
import control.RelatorioController.ResultadoCarteira;
import DTO.CarteiraDTO;
import DTO.MovimentacaoDTO;
import exception.AppException;
import view.opcoes_menus.OpcoesMenuRelatorios;

import java.util.List;
import java.util.Scanner;

public class MenuRelatorios {

    // Códigos ANSI de cor
    private static final String RESET   = "\u001B[0m";
    private static final String VERDE   = "\u001B[32m";
    private static final String VERMELHO = "\u001B[31m";
    private static final String AMARELO = "\u001B[33m";
    private static final String CIANO   = "\u001B[36m";
    private static final String NEGRITO = "\u001B[1m";

    private final Scanner input;
    private final RelatorioController controller;

    public MenuRelatorios(Scanner input, RelatorioController controller) {
        this.input = input;
        this.controller = controller;
    }

    public void exibirMenuRelatorios() {
        while (true) {
            System.out.println(NEGRITO + CIANO + "\n=== RELATÓRIOS ===" + RESET);
            System.out.println("1. Listar carteiras por identificador");
            System.out.println("2. Listar carteiras por nome do titular");
            System.out.println("3. Saldo atual de uma carteira");
            System.out.println("4. Histórico de movimentações de uma carteira");
            System.out.println("5. Ganho/Perda total por carteira");
            System.out.println("0. Voltar");

            OpcoesMenuRelatorios selecao = OpcoesMenuRelatorios.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case LISTAR_POR_ID:
                    listarPorIdentificador();
                    break;
                case LISTAR_POR_NOME:
                    listarPorNome();
                    break;
                case EXIBIR_SALDO:
                    exibirSaldo();
                    break;
                case HISTORICO_MOVIMENTACAO:
                    exibirHistorico();
                    break;
                case GANHO_PERDA:
                    exibirGanhoPerdaPorCarteira();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    System.out.println(AMARELO + "Opção inválida. Tente novamente." + RESET);
                    break;
            }
        }
    }

    //Lista em ordem crescente por ID
    private void listarPorIdentificador() {
        try {
            List<CarteiraDTO> lista = controller.listarCarteiraPorIdentificador();
            System.out.println(NEGRITO + "\n--- Carteiras ordenadas por identificador ---" + RESET);
            imprimirCarteiras(lista);
        } catch (AppException e) {
            System.out.println(VERMELHO + "Erro: " + e.getMessage() + RESET);
        }
    }

    //Lista em ordem crescente por nome
    private void listarPorNome() {
        try {
            List<CarteiraDTO> lista = controller.listarCarteiraPorNomeTitular();
            System.out.println(NEGRITO + "\n--- Carteiras ordenadas por nome do titular ---" + RESET);
            imprimirCarteiras(lista);
        } catch (AppException e) {
            System.out.println(VERMELHO + "Erro: " + e.getMessage() + RESET);
        }
    }

   //Verificar o sado de uma carteira
    private void exibirSaldo() {
        try {
            int id = Main.lerInteiro(input, "Digite o identificador da carteira:");
            double saldo = controller.consultarSaldo(id);
            System.out.println(NEGRITO + "\n--- Saldo da carteira #" + id + " ---" + RESET);
            System.out.printf("Saldo atual: " + CIANO + "%.4f" + RESET + " moedas%n", saldo);
        } catch (AppException e) {
            System.out.println(VERMELHO + "Erro: " + e.getMessage() + RESET);
        }
    }

    //Visualizar todas as movimentações de uma carteira
    private void exibirHistorico() {
        try {
            int id = Main.lerInteiro(input, "Digite o identificador da carteira:");
            List<MovimentacaoDTO> lista = controller.listarHistoricoMovimentacao(id);

            System.out.println(NEGRITO + "\n--- Histórico de movimentações — carteira #" + id + " ---" + RESET);

            if (lista.isEmpty()) {
                System.out.println("Nenhuma movimentação registrada.");
                return;
            }

            System.out.printf("%-6s %-12s %-8s %-12s%n", "Mov#", "Data", "Tipo", "Quantidade");
            System.out.println("------------------------------------------");

            for (MovimentacaoDTO m : lista) {
                String cor = m.getTipo().getCodigo() == 'C' ? VERDE : VERMELHO;
                String tipo = m.getTipo().getCodigo() == 'C' ? "COMPRA" : "VENDA";
                System.out.printf("%-6d %-12s %s%-8s" + RESET + " %-12.4f%n",
                        m.getIdMovimento(),
                        m.getData(),
                        cor,
                        tipo,
                        m.getQuantidade());
            }
        } catch (AppException e) {
            System.out.println(VERMELHO + "Erro: " + e.getMessage() + RESET);
        }
    }

    //Ver a análise de ganho e perda
    private void exibirGanhoPerdaPorCarteira() {
        try {
            List<ResultadoCarteira> resultados = controller.calcularGanhoPerdaPorCarteira();

            System.out.println(NEGRITO + "\n--- Ganho/Perda total por carteira ---" + RESET);

            if (resultados.isEmpty()) {
                System.out.println("Nenhuma carteira cadastrada.");
                return;
            }

            System.out.printf("%-6s %-25s %-20s %-15s%n", "ID", "Titular", "Corretora", "Resultado (R$)");
            System.out.println("------------------------------------------------------------------------");

            for (ResultadoCarteira r : resultados) {
                String cor;
                String sinal;
                if (r.temGanho()) {
                    cor = VERDE;
                    sinal = "+";
                } else if (r.temPerda()) {
                    cor = VERMELHO;
                    sinal = "";
                } else {
                    cor = AMARELO;
                    sinal = " ";
                }

                System.out.printf("%-6d %-25s %-20s %s%s%.2f" + RESET + "%n",
                        r.getCarteira().getIdentificador(),
                        r.getCarteira().getNomeTitular(),
                        r.getCarteira().getCorretora(),
                        cor,
                        sinal,
                        r.getResultado());

                for (String aviso : r.getAvisos()) {
                    System.out.println(AMARELO + "  ⚠ " + aviso + RESET);
                }
            }
        } catch (AppException e) {
            System.out.println(VERMELHO + "Erro: " + e.getMessage() + RESET);
        }
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------
    private void imprimirCarteiras(List<CarteiraDTO> lista) {
        if (lista.isEmpty()) {
            System.out.println("Nenhuma carteira cadastrada.");
            return;
        }
        System.out.printf("%-6s %-25s %-20s%n", "ID", "Titular", "Corretora");
        System.out.println("---------------------------------------------------");
        for (CarteiraDTO c : lista) {
            System.out.printf("%-6d %-25s %-20s%n",
                    c.getIdentificador(),
                    c.getNomeTitular(),
                    c.getCorretora());
        }
    }
}