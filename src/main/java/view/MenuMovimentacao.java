package view;

import controller.MovimentacaoController;
import dto.MovimentacaoDTO;
import exception.AppException;

/**
 * Compra e venda de FTCoins. As operações são registradas na data de hoje, à cotação do dia.
 */
public class MenuMovimentacao extends Menu {

    private final MovimentacaoController controller;

    public MenuMovimentacao(Console console, MovimentacaoController controller) {
        super(console, "Movimentação");
        this.controller = controller;
        adicionarOpcao("Compra de moeda virtual", () -> registrar("Compra", controller::registrarCompra));
        adicionarOpcao("Venda de moeda virtual", () -> registrar("Venda", controller::registrarVenda));
    }

    @FunctionalInterface
    private interface Registro {
        MovimentacaoDTO registrar(int idCarteira, double quantidade) throws AppException;
    }

    private void registrar(String operacao, Registro registro) throws AppException {
        int idCarteira = console.lerInteiro("Digite o identificador da carteira:");
        controller.validarCarteiraExiste(idCarteira);

        double cotacao = controller.cotacaoDeHoje();
        console.exibir("Cotação de hoje: " + Formatador.moeda(cotacao));
        double quantidade = console.lerDecimal("Digite a quantidade:");

        MovimentacaoDTO movimentacao = registro.registrar(idCarteira, quantidade);
        console.exibir(operacao + " registrada com sucesso: "
                + Formatador.quantidade(movimentacao.getQuantidade()) + " FTCoins em "
                + Formatador.data(movimentacao.getData()) + " a " + Formatador.moeda(cotacao)
                + " (total " + Formatador.moeda(movimentacao.getQuantidade() * cotacao) + ").");
    }
}
