package view;

import controller.RelatorioController;
import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import exception.AppException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Relatórios das carteiras: listagens, saldo atual, histórico, evolução diária e ganho/perda.
 */
public class MenuRelatorios extends Menu {

    private static final String FORMATO_HISTORICO = "%-10s %-7s %12s %10s %12s %12s %12s %14s %14s%n";
    private static final String FORMATO_EVOLUCAO = "%-10s %9s %9s %9s %11s %11s %14s %13s %14s %14s%n";

    private final RelatorioController controller;

    public MenuRelatorios(Console console, RelatorioController controller) {
        super(console, "Relatórios");
        this.controller = controller;
        adicionarOpcao("Listar carteiras por identificador",
                () -> exibirCarteiras(controller.listarPorIdentificador(), "identificador"));
        adicionarOpcao("Listar carteiras por titular",
                () -> exibirCarteiras(controller.listarPorTitular(), "titular"));
        adicionarOpcao("Saldo atual", this::exibirSaldo);
        adicionarOpcao("Histórico de movimentação", this::exibirHistorico);
        adicionarOpcao("Evolução diária", this::exibirEvolucaoDiaria);
        adicionarOpcao("Ganhos e perdas", this::exibirGanhoPerda);
    }

    private void exibirCarteiras(List<CarteiraDTO> carteiras, String criterio) {
        if (carteiras.isEmpty()) {
            console.exibir("Nenhuma carteira cadastrada.");
            return;
        }
        console.exibir("Carteiras ordenadas por " + criterio + ":");
        for (CarteiraDTO carteira : carteiras) {
            console.exibir("  " + Formatador.carteira(carteira));
        }
    }

    private void exibirSaldo() throws AppException {
        int identificador = console.lerInteiro("Digite o identificador da carteira:");
        ApuracaoDTO apuracao = controller.apurar(identificador);

        List<String> linhas = new ArrayList<>();
        linhas.add("Saldo atual da carteira " + identificador + " em "
                + Formatador.data(apuracao.dataCotacaoAtual()) + ":");
        linhas.add(campo("Quantidade (FTCoins)", Formatador.quantidade(apuracao.saldoQuantidade())));
        linhas.add(campo("Custo médio", Formatador.moeda(apuracao.custoMedio())));
        linhas.add(campo("Valor a custo médio", Formatador.moeda(apuracao.saldoCustoMedio())));
        linhas.add(campo("Cotação atual", Formatador.moeda(apuracao.cotacaoAtual())));
        linhas.add(campo("Valor a mercado", Formatador.moeda(apuracao.saldoMercado())));
        linhas.add(campo("Saldo em reais", Formatador.moeda(apuracao.saldoReais())));
        linhas.forEach(console::exibir);

        if (console.confirmar("Deseja imprimir este saldo em arquivo?")) {
            Path arquivo = console.salvarArquivo(
                    "saldo-carteira-" + identificador + "-" + apuracao.dataCotacaoAtual() + ".txt", linhas);
            console.exibir("Saldo impresso em " + arquivo);
        }
    }

    private void exibirHistorico() throws AppException {
        int identificador = console.lerInteiro("Digite o identificador da carteira:");
        List<ApuracaoDTO.Linha> linhas = controller.apurar(identificador).linhas();
        if (linhas.isEmpty()) {
            console.exibir("Nenhuma movimentação registrada para a carteira " + identificador + ".");
            return;
        }
        console.exibir("Histórico da carteira " + identificador + " (valores em R$):");
        console.exibirFormatado(FORMATO_HISTORICO, "Data", "Tipo", "Qtd.", "Cotação", "Valor", "Saldo (qtd)",
                "Custo médio", "Res. operação", "Saldo (R$)");
        for (ApuracaoDTO.Linha linha : linhas) {
            boolean ftcoins = linha.tipo().movimentaFTCoins();
            console.exibirFormatado(FORMATO_HISTORICO,
                    Formatador.data(linha.data()),
                    linha.tipo().getDescricao(),
                    ftcoins ? Formatador.quantidade(linha.quantidade()) : "-",
                    ftcoins ? Formatador.numero(linha.cotacao()) : "-",
                    Formatador.numero(linha.valor()),
                    Formatador.quantidade(linha.saldoQuantidade()),
                    Formatador.numero(linha.custoMedio()),
                    Formatador.numero(linha.resultadoOperacao()),
                    Formatador.numero(linha.saldoReais()));
        }
    }

    private void exibirEvolucaoDiaria() throws AppException {
        int identificador = console.lerInteiro("Digite o identificador da carteira:");
        List<ApuracaoDTO.Dia> dias = controller.evolucaoDiaria(identificador);
        if (dias.isEmpty()) {
            console.exibir("Nenhuma movimentação registrada para a carteira " + identificador + ".");
            return;
        }
        console.exibir("Evolução diária da carteira " + identificador + " (valores em R$):");
        console.exibirFormatado(FORMATO_EVOLUCAO, "Data", "Cotação", "Compra", "Venda", "Saldo (qtd)",
                "Custo médio", "Sd. financeiro", "Res. operação", "Res. acumulado", "Sd. mercado");
        double quantidadeComprada = 0;
        double quantidadeVendida = 0;
        double valorComprado = 0;
        double valorVendido = 0;
        for (ApuracaoDTO.Dia dia : dias) {
            console.exibirFormatado(FORMATO_EVOLUCAO,
                    Formatador.data(dia.data()),
                    Formatador.numero(dia.cotacao()),
                    Formatador.quantidade(dia.quantidadeComprada()),
                    Formatador.quantidade(dia.quantidadeVendida()),
                    Formatador.quantidade(dia.saldoQuantidade()),
                    Formatador.numero(dia.custoMedio()),
                    Formatador.numero(dia.saldoCustoMedio()),
                    Formatador.numero(dia.resultadoOperacao()),
                    Formatador.numero(dia.resultadoAcumulado()),
                    Formatador.numero(dia.saldoMercado()));
            quantidadeComprada += dia.quantidadeComprada();
            quantidadeVendida += dia.quantidadeVendida();
            valorComprado += dia.valorComprado();
            valorVendido += dia.valorVendido();
        }
        console.exibir("Totais: compras de " + Formatador.quantidade(quantidadeComprada) + " FTCoins ("
                + Formatador.moeda(valorComprado) + "), vendas de " + Formatador.quantidade(quantidadeVendida)
                + " FTCoins (" + Formatador.moeda(valorVendido) + ").");
    }

    private void exibirGanhoPerda() throws AppException {
        List<CarteiraDTO> carteiras = controller.listarPorIdentificador();
        if (carteiras.isEmpty()) {
            console.exibir("Nenhuma carteira cadastrada.");
            return;
        }
        for (CarteiraDTO carteira : carteiras) {
            console.exibir("\n" + Formatador.carteira(carteira));
            try {
                exibirResumo(controller.apurar(carteira.getIdentificador()));
            } catch (AppException e) {
                // Uma carteira com problema não impede o relatório das demais.
                console.exibir("  " + e.getMessage());
            }
        }
    }

    private void exibirResumo(ApuracaoDTO apuracao) {
        if (apuracao.linhas().isEmpty()) {
            console.exibir("  Nenhuma movimentação registrada.");
            return;
        }
        console.exibir("  Você vendeu suas moedas por " + Formatador.moeda(apuracao.totalVendido())
                + " e as havia comprado por " + Formatador.moeda(apuracao.totalComprado()) + ".");
        console.exibir("  Teve " + (apuracao.resultadoOperacional() >= 0 ? "lucro" : "prejuízo")
                + " operacional de " + Formatador.moeda(apuracao.resultadoOperacional()) + ".");
        console.exibir("  Você tem, a custo médio, moedas no valor de "
                + Formatador.moeda(apuracao.saldoCustoMedio()) + ".");
        console.exibir("  Portanto, seu resultado geral é de "
                + Formatador.resultado(apuracao.resultadoCustoMedio(), apuracao.percentualCustoMedio()) + ".");
        console.exibir("  Se vender todas as moedas na última cotação (" + Formatador.moeda(apuracao.cotacaoAtual())
                + " em " + Formatador.data(apuracao.dataCotacaoAtual()) + "), seu resultado passaria a ser "
                + Formatador.resultado(apuracao.resultadoMercado(), apuracao.percentualMercado()) + ".");
        console.exibir("  Saldo em reais disponível na carteira: " + Formatador.moeda(apuracao.saldoReais()) + ".");
    }

    private static String campo(String rotulo, String valor) {
        return String.format("  %-24s: %s", rotulo, valor);
    }
}
