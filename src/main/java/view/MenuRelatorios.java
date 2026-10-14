package view;

import controller.RelatorioController;
import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import exception.AppException;
import java.util.List;
import model.TipoMovimentacao;

/**
 * Relatórios das carteiras: listagens, saldo atual, histórico e ganho/perda.
 */
public class MenuRelatorios extends Menu {

    private static final String FORMATO_TABELA = "%-10s %-6s %12s %13s %14s %12s %13s %15s%n";

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
        console.exibir("Saldo atual da carteira " + identificador + ":");
        exibirCampo("Quantidade (FTCoins)", Formatador.quantidade(apuracao.saldoQuantidade()));
        exibirCampo("Custo médio", Formatador.moeda(apuracao.custoMedio()));
        exibirCampo("Valor a custo médio", Formatador.moeda(apuracao.saldoCustoMedio()));
        exibirCampo("Cotação atual", Formatador.moeda(apuracao.cotacaoAtual()));
        exibirCampo("Valor a mercado", Formatador.moeda(apuracao.saldoMercado()));
    }

    private void exibirHistorico() throws AppException {
        int identificador = console.lerInteiro("Digite o identificador da carteira:");
        List<ApuracaoDTO.Linha> linhas = controller.apurar(identificador).linhas();
        if (linhas.isEmpty()) {
            console.exibir("Nenhuma movimentação registrada para a carteira " + identificador + ".");
            return;
        }
        console.exibir("Histórico da carteira " + identificador + " (valores em R$):");
        console.exibirFormatado(FORMATO_TABELA, "Data", "Tipo", "Qtd.", "Cotação", "Valor", "Saldo (qtd)",
                "Custo médio", "Res. operação");
        for (ApuracaoDTO.Linha linha : linhas) {
            console.exibirFormatado(FORMATO_TABELA,
                    Formatador.data(linha.data()),
                    linha.tipo() == TipoMovimentacao.COMPRA ? "Compra" : "Venda",
                    Formatador.quantidade(linha.quantidade()),
                    Formatador.numero(linha.cotacao()),
                    Formatador.numero(linha.valor()),
                    Formatador.quantidade(linha.saldoQuantidade()),
                    Formatador.numero(linha.custoMedio()),
                    Formatador.numero(linha.resultadoOperacao()));
        }
    }

    private void exibirGanhoPerda() throws AppException {
        List<CarteiraDTO> carteiras = controller.listarPorIdentificador();
        if (carteiras.isEmpty()) {
            console.exibir("Nenhuma carteira cadastrada.");
            return;
        }
        console.exibir("Ganho/perda por carteira:");
        for (CarteiraDTO carteira : carteiras) {
            console.exibir("\n" + Formatador.carteira(carteira));
            try {
                exibirResultados(controller.apurar(carteira.getIdentificador()));
            } catch (AppException e) {
                // Uma carteira com problema não impede o relatório das demais.
                console.exibir("  " + e.getMessage());
            }
        }
    }

    private void exibirResultados(ApuracaoDTO apuracao) {
        exibirCampo("Total comprado", Formatador.moeda(apuracao.totalComprado()));
        exibirCampo("Total vendido", Formatador.moeda(apuracao.totalVendido()));
        exibirCampo(apuracao.resultadoOperacional() >= 0 ? "Lucro operacional" : "Prejuízo operacional",
                Formatador.moeda(apuracao.resultadoOperacional()));
        exibirCampo("Saldo (FTCoins)", Formatador.quantidade(apuracao.saldoQuantidade()));
        exibirCampo("Valor a custo médio", Formatador.moeda(apuracao.saldoCustoMedio()));
        exibirCampo("Valor a mercado", Formatador.moeda(apuracao.saldoMercado())
                + " (cotação atual " + Formatador.moeda(apuracao.cotacaoAtual()) + ")");
        exibirCampo("Resultado a custo médio",
                Formatador.resultado(apuracao.resultadoCustoMedio(), apuracao.percentualCustoMedio()));
        exibirCampo("Resultado a mercado",
                Formatador.resultado(apuracao.resultadoMercado(), apuracao.percentualMercado()));
    }

    private void exibirCampo(String rotulo, String valor) {
        console.exibirFormatado("  %-24s: %s%n", rotulo, valor);
    }
}
