package view;

import controller.OraculoController;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;

/**
 * Consulta das cotações diárias do Oráculo.
 */
public class MenuOraculo extends Menu {

    private final OraculoController controller;

    public MenuOraculo(Console console, OraculoController controller) {
        super(console, "Oráculo");
        this.controller = controller;
        adicionarOpcao("Cotação de hoje", this::exibirCotacaoDeHoje);
        adicionarOpcao("Consultar cotação por data", this::consultarCotacao);
        adicionarOpcao("Listar cotações por período", this::listarCotacoes);
    }

    private void exibirCotacaoDeHoje() throws AppException {
        exibirCotacao(controller.cotacaoDeHoje());
    }

    private void consultarCotacao() throws AppException {
        LocalDate data = console.lerData("Digite a data da cotação");
        exibirCotacao(controller.consultar(data));
    }

    private void listarCotacoes() throws AppException {
        LocalDate inicio = console.lerData("Digite a data inicial");
        LocalDate fim = console.lerData("Digite a data final");
        List<OraculoDTO> cotacoes = controller.listarPeriodo(inicio, fim);
        console.exibir("Cotações de " + Formatador.data(inicio) + " a " + Formatador.data(fim) + ":");
        for (OraculoDTO cotacao : cotacoes) {
            exibirCotacao(cotacao);
        }
    }

    private void exibirCotacao(OraculoDTO cotacao) {
        console.exibir("  " + Formatador.data(cotacao.getData()) + "  " + Formatador.moeda(cotacao.getCotacao()));
    }
}
