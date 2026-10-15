package view;

import config.Mensagens;
import controller.MovimentacaoController;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.util.Optional;

/**
 * Compra e venda de FTCoins, crédito e saque em reais. As operações são registradas na data de
 * hoje, à cotação do dia.
 */
public class MenuMovimentacao extends Menu {

    private final MovimentacaoController controller;

    public MenuMovimentacao(Console console, MovimentacaoController controller) {
        super(console, Mensagens.get("movimentacao.titulo"));
        this.controller = controller;
        adicionarOpcao(Mensagens.get("movimentacao.compra"),
                () -> negociar("movimentacao.compraRegistrada", controller::registrarCompra));
        adicionarOpcao(Mensagens.get("movimentacao.venda"),
                () -> negociar("movimentacao.vendaRegistrada", controller::registrarVenda));
        adicionarOpcao(Mensagens.get("movimentacao.credito"),
                () -> movimentarReais("movimentacao.pedirCredito", "movimentacao.creditoRegistrado", controller::creditar));
        adicionarOpcao(Mensagens.get("movimentacao.saque"),
                () -> movimentarReais("movimentacao.pedirSaque", "movimentacao.saqueRegistrado", controller::sacar));
    }

    @FunctionalInterface
    private interface Registro {
        MovimentacaoDTO registrar(int idCarteira, double quantidade) throws AppException;
    }

    private void negociar(String chaveSucesso, Registro registro) throws AppException {
        Optional<CarteiraDTO> carteira = escolherCarteira(controller.listarCarteiras());
        if (carteira.isEmpty()) {
            return;
        }
        int idCarteira = carteira.get().getIdentificador();
        double cotacao = controller.cotacaoDeHoje();
        console.exibir(Mensagens.get("movimentacao.cotacaoESaldo", Formatador.moeda(cotacao),
                Formatador.moeda(controller.saldoEmReais(idCarteira))));
        double quantidade = console.lerDecimal(Mensagens.get("movimentacao.pedirQuantidade"));

        MovimentacaoDTO movimentacao = registro.registrar(idCarteira, quantidade);
        console.exibir(Mensagens.get(chaveSucesso, Formatador.quantidade(movimentacao.getQuantidade()),
                Formatador.data(movimentacao.getData()), Formatador.moeda(cotacao),
                Formatador.moeda(movimentacao.getQuantidade() * cotacao)));
        exibirSaldoEmReais(idCarteira);
    }

    private void movimentarReais(String chavePedido, String chaveSucesso, Registro registro) throws AppException {
        Optional<CarteiraDTO> carteira = escolherCarteira(controller.listarCarteiras());
        if (carteira.isEmpty()) {
            return;
        }
        int idCarteira = carteira.get().getIdentificador();
        exibirSaldoEmReais(idCarteira);
        double valor = console.lerDecimal(Mensagens.get(chavePedido));

        registro.registrar(idCarteira, valor);
        console.exibir(Mensagens.get(chaveSucesso, Formatador.moeda(valor)));
        exibirSaldoEmReais(idCarteira);
    }

    private void exibirSaldoEmReais(int idCarteira) throws AppException {
        console.exibir(Mensagens.get("movimentacao.saldoReais", Formatador.moeda(controller.saldoEmReais(idCarteira))));
    }
}
