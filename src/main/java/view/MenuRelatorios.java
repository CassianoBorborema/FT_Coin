package view;

import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import dto.LinhaApuracaoDTO;
import app.Main;
import controller.RelatorioController;
import exception.AppException;
import model.TipoMovimentacao;
import view.opcoes_menus.OpcoesMenuRelatorios;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class MenuRelatorios {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String FORMATO_TABELA = "%-10s %-6s %12s %13s %14s %12s %13s %15s%n";
    private static final double ARREDONDA_ZERO = 0.005;

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
            ApuracaoDTO apuracao = controller.apurar(identificador);
            System.out.println("Saldo atual da carteira " + identificador + ":");
            exibirCampo("Quantidade (FTCoins)", formatarQuantidade(apuracao.getSaldoQuantidade()));
            exibirCampo("Custo médio", formatarMoeda(apuracao.getCustoMedio()));
            exibirCampo("Valor a custo médio", formatarMoeda(apuracao.getSaldoCustoMedio()));
            exibirCampo("Cotação atual", formatarMoeda(apuracao.getCotacaoAtual()));
            exibirCampo("Valor a mercado", formatarMoeda(apuracao.getSaldoMercado()));
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirHistorico() {
        try {
            int identificador = Main.lerInteiro(input, "Digite o identificador da carteira:");
            List<LinhaApuracaoDTO> linhas = controller.apurar(identificador).getLinhas();
            if (linhas.isEmpty()) {
                System.out.println("Nenhuma movimentação registrada para a carteira " + identificador + ".");
                return;
            }
            System.out.println("Histórico da carteira " + identificador + " (valores em R$):");
            System.out.printf(FORMATO_TABELA, "Data", "Tipo", "Qtd.", "Cotação", "Valor", "Saldo (qtd)",
                    "Custo médio", "Res. operação");
            for (LinhaApuracaoDTO linha : linhas) {
                System.out.printf(FORMATO_TABELA,
                        linha.getData().format(FORMATO_DATA),
                        linha.getTipo() == TipoMovimentacao.COMPRA ? "Compra" : "Venda",
                        formatarQuantidade(linha.getQuantidade()),
                        formatarNumero(linha.getCotacao()),
                        formatarNumero(linha.getValor()),
                        formatarQuantidade(linha.getSaldoQuantidade()),
                        formatarNumero(linha.getCustoMedio()),
                        formatarNumero(linha.getResultadoOperacao()));
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
                System.out.println("\nCarteira " + id + " (" + carteira.getNomeTitular() + ")");
                try {
                    exibirResultados(controller.apurar(id));
                } catch (AppException e) {
                    System.out.println("  " + e.getMessage());
                }
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void exibirResultados(ApuracaoDTO apuracao) {
        exibirCampo("Total comprado", formatarMoeda(apuracao.getTotalComprado()));
        exibirCampo("Total vendido", formatarMoeda(apuracao.getTotalVendido()));
        exibirCampo(apuracao.getResultadoOperacional() >= 0 ? "Lucro operacional" : "Prejuízo operacional",
                formatarMoeda(apuracao.getResultadoOperacional()));
        exibirCampo("Saldo (FTCoins)", formatarQuantidade(apuracao.getSaldoQuantidade()));
        exibirCampo("Valor a custo médio", formatarMoeda(apuracao.getSaldoCustoMedio()));
        exibirCampo("Valor a mercado", formatarMoeda(apuracao.getSaldoMercado())
                + " (cotação atual " + formatarMoeda(apuracao.getCotacaoAtual()) + ")");
        exibirCampo("Resultado a custo médio", formatarResultado(
                apuracao.getResultadoCustoMedio(), apuracao.getPercentualCustoMedio()));
        exibirCampo("Resultado a mercado", formatarResultado(
                apuracao.getResultadoMercado(), apuracao.getPercentualMercado()));
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

    private void exibirCampo(String rotulo, String valor) {
        System.out.printf("  %-24s: %s%n", rotulo, valor);
    }

    private String formatarResultado(double resultado, double percentual) {
        return formatarMoeda(resultado) + " (" + String.format("%.2f%%", zerarSeInsignificante(percentual)) + ")";
    }

    private String formatarMoeda(double valor) {
        return "R$ " + formatarNumero(valor);
    }

    private String formatarNumero(double valor) {
        return String.format("%,.2f", zerarSeInsignificante(valor));
    }

    private String formatarQuantidade(double quantidade) {
        return new DecimalFormat("#,##0.########").format(quantidade);
    }

    /** Evita exibir "-0,00" para valores que arredondam para zero. */
    private double zerarSeInsignificante(double valor) {
        return Math.abs(valor) < ARREDONDA_ZERO ? 0 : valor;
    }
}
