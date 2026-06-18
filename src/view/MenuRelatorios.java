package view;

import DTO.CarteiraDTO;
import DTO.MovimentacaoDTO;
import app.Main;
import controller.RelatorioController;
import exception.AppException;
import java.util.List;
import java.util.Scanner;
import view.opcoes_menus.OpcoesMenuRelatorios;

public class MenuRelatorios {

    private final Scanner input;
    private final RelatorioController controller;

    public MenuRelatorios(Scanner input, RelatorioController controller) {
        this.input      = input;
        this.controller = controller;
    }

    public void exibirMenuRelatorios() {
        while (true) {
            System.out.println("\nQual opcao deseja?");
            System.out.println("1. Listar carteiras por identificador");
            System.out.println("2. Listar carteiras por nome do titular");
            System.out.println("3. Exibir saldo atual de uma carteira");
            System.out.println("4. Historico de movimentacao de uma carteira");
            System.out.println("5. Ganho ou perda total de cada carteira");
            System.out.println("0. Voltar");

            OpcoesMenuRelatorios selecao = OpcoesMenuRelatorios.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case LISTAR_POR_ID:
                    listarPorIdentificador();
                    break;
                case LISTAR_POR_NOME:
                    listarPorNomeTitular();
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
                    System.out.println("Opcao invalida. Tente novamente.");
                    break;
            }
        }
    }

    private void listarPorIdentificador() {
        try {
            List<CarteiraDTO> carteiras = controller.listarPorIdentificador();
            if (carteiras.isEmpty()) {
                System.out.println("Nenhuma carteira cadastrada.");
                return;
            }
            System.out.println("\n=== Carteiras ordenadas por identificador ===");
            System.out.printf("%-6s %-30s %-20s%n", "ID", "Titular", "Corretora");
            System.out.println(linha(58));
            for (CarteiraDTO c : carteiras) {
                System.out.printf("%-6d %-30s %-20s%n",
                        c.getIdentificador(), c.getNomeTitular(), c.getCorretora());
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void listarPorNomeTitular() {
        try {
            List<CarteiraDTO> carteiras = controller.listarPorNomeTitular();
            if (carteiras.isEmpty()) {
                System.out.println("Nenhuma carteira cadastrada.");
                return;
            }
            System.out.println("\n=== Carteiras ordenadas por nome do titular ===");
            System.out.printf("%-30s %-6s %-20s%n", "Titular", "ID", "Corretora");
            System.out.println(linha(58));
            for (CarteiraDTO c : carteiras) {
                System.out.printf("%-30s %-6d %-20s%n",
                        c.getNomeTitular(), c.getIdentificador(), c.getCorretora());
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirSaldo() {
        try {
            int id = Main.lerInteiro(input, "Digite o identificador da carteira:");
            double saldo       = controller.calcularSaldo(id);
            Double valorBRL    = controller.calcularValorAtualBRL(id);

            System.out.println("\n=== Saldo da Carteira #" + id + " ===");
            System.out.printf("Saldo em moedas  : %.6f FTC%n", saldo);
            if (valorBRL != null) {
                System.out.printf("Valor atual (BRL): R$ %.2f%n", valorBRL);
            } else {
                System.out.println("Valor atual (BRL): cotacao de hoje indisponivel no Oraculo.");
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirHistorico() {
        try {
            int id = Main.lerInteiro(input, "Digite o identificador da carteira:");
            List<MovimentacaoDTO> historico = controller.listarHistorico(id);

            if (historico.isEmpty()) {
                System.out.println("Nenhuma movimentacao registrada para a carteira #" + id + ".");
                return;
            }
            System.out.println("\n=== Historico de movimentacoes - Carteira #" + id + " ===");
            System.out.printf("%-8s %-12s %-8s %-16s%n", "Mov.ID", "Data", "Tipo", "Quantidade");
            System.out.println(linha(46));
            for (MovimentacaoDTO m : historico) {
                System.out.printf("%-8d %-12s %-8s %.6f%n",
                        m.getIdMovimento(),
                        m.getData().toString(),
                        m.getTipo().name(),
                        m.getQuantidade());
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirGanhoPerda() {
        try {
            List<RelatorioController.ResultadoGanhoPerda> resultados = controller.calcularGanhoPerdaTodas();
            if (resultados.isEmpty()) {
                System.out.println("Nenhuma carteira cadastrada.");
                return;
            }
            System.out.println("\n=== Ganho / Perda total por carteira ===");
            for (RelatorioController.ResultadoGanhoPerda r : resultados) {
                System.out.println("\nCarteira #" + r.getCarteira().getIdentificador()
                        + " - " + r.getCarteira().getNomeTitular()
                        + " (" + r.getCarteira().getCorretora() + ")");
                System.out.printf("  Total investido (compras)  : R$ %10.2f%n", r.getTotalInvestido());
                System.out.printf("  Total recuperado (vendas)  : R$ %10.2f%n", r.getTotalRecuperado());
                System.out.printf("  Saldo atual (moedas)       :    %10.6f FTC%n", r.getSaldoAtualCoins());
                if (r.isTemCotacaoHoje()) {
                    System.out.printf("  Valor atual das moedas     : R$ %10.2f%n", r.getValorAtualBRL());
                } else {
                    System.out.println("  Valor atual das moedas     : cotacao de hoje indisponivel.");
                }
                double gp    = r.getGanhoPerda();
                String sinal = gp >= 0 ? "+" : "";
                System.out.printf("  Resultado (Ganho/Perda)    : R$ %s%.2f%n", sinal, gp);
                if (r.getCotacoesAusentes() > 0) {
                    System.out.println("  Aviso: " + r.getCotacoesAusentes()
                            + " movimentacao(oes) sem cotacao na data - ignorada(s) no calculo.");
                }
                System.out.println("  " + linha(44));
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    /** Gera uma linha separadora de n caracteres '-'. Compativel com Java 8. */
    private static String linha(int n) {
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            sb.append('-');
        }
        return sb.toString();
    }
}