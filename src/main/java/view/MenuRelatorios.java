package view;

import config.Mensagens;
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

    private static final String FORMATO_HISTORICO = "%-10s %-10s %12s %10s %12s %12s %12s %14s %14s%n";
    private static final String FORMATO_EVOLUCAO = "%-10s %9s %9s %9s %11s %11s %14s %13s %14s %14s%n";

    private final RelatorioController controller;

    public MenuRelatorios(Console console, RelatorioController controller) {
        super(console, Mensagens.get("relatorios.titulo"));
        this.controller = controller;
        adicionarOpcao(Mensagens.get("relatorios.listarPorId"),
                () -> exibirCarteiras(controller.listarPorIdentificador(), "relatorios.ordenadasPorId"));
        adicionarOpcao(Mensagens.get("relatorios.listarPorTitular"),
                () -> exibirCarteiras(controller.listarPorTitular(), "relatorios.ordenadasPorTitular"));
        adicionarOpcao(Mensagens.get("relatorios.saldo"), this::exibirSaldo);
        adicionarOpcao(Mensagens.get("relatorios.historico"), this::exibirHistorico);
        adicionarOpcao(Mensagens.get("relatorios.evolucao"), this::exibirEvolucaoDiaria);
        adicionarOpcao(Mensagens.get("relatorios.ganhoPerda"), this::exibirGanhoPerda);
    }

    private void exibirCarteiras(List<CarteiraDTO> carteiras, String chaveTitulo) {
        if (carteiras.isEmpty()) {
            console.exibir(Mensagens.get("relatorios.semCarteiras"));
            return;
        }
        console.exibir(Mensagens.get(chaveTitulo));
        for (CarteiraDTO carteira : carteiras) {
            console.exibir("  " + Formatador.carteira(carteira));
        }
    }

    private void exibirSaldo() throws AppException {
        int identificador = console.lerInteiro(Mensagens.get("pedir.carteira"));
        ApuracaoDTO apuracao = controller.apurar(identificador);

        List<String> linhas = new ArrayList<>();
        linhas.add(Mensagens.get("saldo.cabecalho", identificador, apuracao.dataCotacaoAtual()));
        linhas.add(campo("saldo.quantidade", Formatador.quantidade(apuracao.saldoQuantidade())));
        linhas.add(campo("saldo.custoMedio", Formatador.moeda(apuracao.custoMedio())));
        linhas.add(campo("saldo.valorCustoMedio", Formatador.moeda(apuracao.saldoCustoMedio())));
        linhas.add(campo("saldo.cotacaoAtual", Formatador.moeda(apuracao.cotacaoAtual())));
        linhas.add(campo("saldo.valorMercado", Formatador.moeda(apuracao.saldoMercado())));
        linhas.add(campo("saldo.reais", Formatador.moeda(apuracao.saldoReais())));
        linhas.forEach(console::exibir);

        if (console.confirmar(Mensagens.get("saldo.imprimir"))) {
            Path arquivo = console.salvarArquivo(
                    Mensagens.get("saldo.arquivo", identificador, apuracao.dataCotacaoAtual().toString()), linhas);
            console.exibir(Mensagens.get("saldo.impresso", arquivo.toString()));
        }
    }

    private void exibirHistorico() throws AppException {
        int identificador = console.lerInteiro(Mensagens.get("pedir.carteira"));
        List<ApuracaoDTO.Linha> linhas = controller.apurar(identificador).linhas();
        if (linhas.isEmpty()) {
            console.exibir(Mensagens.get("relatorios.semMovimentacoes", identificador));
            return;
        }
        console.exibir(Mensagens.get("historico.titulo", identificador));
        console.exibirFormatado(FORMATO_HISTORICO, colunas("data", "tipo", "quantidade", "cotacao", "valor",
                "saldoQuantidade", "custoMedio", "resultadoOperacao", "saldoReais"));
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
        int identificador = console.lerInteiro(Mensagens.get("pedir.carteira"));
        List<ApuracaoDTO.Dia> dias = controller.evolucaoDiaria(identificador);
        if (dias.isEmpty()) {
            console.exibir(Mensagens.get("relatorios.semMovimentacoes", identificador));
            return;
        }
        console.exibir(Mensagens.get("evolucao.titulo", identificador));
        console.exibirFormatado(FORMATO_EVOLUCAO, colunas("data", "cotacao", "compra", "venda", "saldoQuantidade",
                "custoMedio", "saldoFinanceiro", "resultadoOperacao", "resultadoAcumulado", "saldoMercado"));
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
        console.exibir(Mensagens.get("evolucao.totais", Formatador.quantidade(quantidadeComprada),
                Formatador.moeda(valorComprado), Formatador.quantidade(quantidadeVendida),
                Formatador.moeda(valorVendido)));
    }

    private void exibirGanhoPerda() throws AppException {
        List<CarteiraDTO> carteiras = controller.listarPorIdentificador();
        if (carteiras.isEmpty()) {
            console.exibir(Mensagens.get("relatorios.semCarteiras"));
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
            console.exibir("  " + Mensagens.get("resumo.semMovimentacoes"));
            return;
        }
        exibirLinhaResumo("resumo.vendidoComprado", Formatador.moeda(apuracao.totalVendido()),
                Formatador.moeda(apuracao.totalComprado()));
        exibirLinhaResumo(apuracao.resultadoOperacional() >= 0 ? "resumo.lucro" : "resumo.prejuizo",
                Formatador.moeda(apuracao.resultadoOperacional()));
        exibirLinhaResumo("resumo.custoMedio", Formatador.moeda(apuracao.saldoCustoMedio()));
        exibirLinhaResumo("resumo.resultadoGeral",
                Formatador.resultado(apuracao.resultadoCustoMedio(), apuracao.percentualCustoMedio()));
        exibirLinhaResumo("resumo.resultadoMercado", Formatador.moeda(apuracao.cotacaoAtual()),
                Formatador.data(apuracao.dataCotacaoAtual()),
                Formatador.resultado(apuracao.resultadoMercado(), apuracao.percentualMercado()));
        exibirLinhaResumo("resumo.saldoReais", Formatador.moeda(apuracao.saldoReais()));
    }

    private void exibirLinhaResumo(String chave, Object... argumentos) {
        console.exibir("  " + Mensagens.get(chave, argumentos));
    }

    private static Object[] colunas(String... nomes) {
        Object[] titulos = new Object[nomes.length];
        for (int i = 0; i < nomes.length; i++) {
            titulos[i] = Mensagens.get("coluna." + nomes[i]);
        }
        return titulos;
    }

    private static String campo(String chave, String valor) {
        return String.format("  %-24s: %s", Mensagens.get(chave), valor);
    }
}
