package view;

import controller.MovimentacaoController;
import dto.MovimentacaoDTO;
import exception.AppException;

/**
 * Compra e venda de FTCoins, crédito e saque em reais. As operações são registradas na data de
 * hoje, à cotação do dia.
 */
public class MenuMovimentacao extends Menu {

    private final MovimentacaoController controller;

    public MenuMovimentacao(Console console, MovimentacaoController controller) {
        super(console, "Movimentação");
        this.controller = controller;
        adicionarOpcao("Compra de moeda virtual", () -> negociar("Compra", controller::registrarCompra));
        adicionarOpcao("Venda de moeda virtual", () -> negociar("Venda", controller::registrarVenda));
        adicionarOpcao("Crédito em reais", () -> movimentarReais("Crédito", "creditar", controller::creditar));
        adicionarOpcao("Saque em reais", () -> movimentarReais("Saque", "sacar", controller::sacar));
    }

    @FunctionalInterface
    private interface Registro {
        MovimentacaoDTO registrar(int idCarteira, double quantidade) throws AppException;
    }

    private void negociar(String operacao, Registro registro) throws AppException {
        int idCarteira = lerCarteira();
        double cotacao = controller.cotacaoDeHoje();
        console.exibir("Cotação de hoje: " + Formatador.moeda(cotacao)
                + " | Saldo em reais: " + Formatador.moeda(controller.saldoEmReais(idCarteira)));
        double quantidade = console.lerDecimal("Digite a quantidade de FTCoins:");

        MovimentacaoDTO movimentacao = registro.registrar(idCarteira, quantidade);
        console.exibir(operacao + " registrada com sucesso: "
                + Formatador.quantidade(movimentacao.getQuantidade()) + " FTCoins em "
                + Formatador.data(movimentacao.getData()) + " a " + Formatador.moeda(cotacao)
                + " (total " + Formatador.moeda(movimentacao.getQuantidade() * cotacao) + ").");
        exibirSaldoEmReais(idCarteira);
    }

    private void movimentarReais(String operacao, String verbo, Registro registro) throws AppException {
        int idCarteira = lerCarteira();
        console.exibir("Saldo em reais: " + Formatador.moeda(controller.saldoEmReais(idCarteira)));
        double valor = console.lerDecimal("Digite o valor a " + verbo + " (R$):");

        registro.registrar(idCarteira, valor);
        console.exibir(operacao + " de " + Formatador.moeda(valor) + " registrado com sucesso.");
        exibirSaldoEmReais(idCarteira);
    }

    private int lerCarteira() throws AppException {
        int idCarteira = console.lerInteiro("Digite o identificador da carteira:");
        controller.validarCarteiraExiste(idCarteira);
        return idCarteira;
    }

    private void exibirSaldoEmReais(int idCarteira) throws AppException {
        console.exibir("Saldo em reais: " + Formatador.moeda(controller.saldoEmReais(idCarteira)));
    }
}
