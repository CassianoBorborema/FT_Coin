package view;

import config.Mensagens;
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
        super(console, Mensagens.get("oraculo.titulo"));
        this.controller = controller;
        adicionarOpcao(Mensagens.get("oraculo.hoje"), this::exibirCotacaoDeHoje);
        adicionarOpcao(Mensagens.get("oraculo.porData"), this::consultarCotacao);
        adicionarOpcao(Mensagens.get("oraculo.porPeriodo"), this::listarCotacoes);
    }

    private void exibirCotacaoDeHoje() throws AppException {
        exibirCotacao(controller.cotacaoDeHoje());
    }

    private void consultarCotacao() throws AppException {
        LocalDate data = console.lerData(Mensagens.get("oraculo.pedirData"));
        exibirCotacao(controller.consultar(data));
    }

    private void listarCotacoes() throws AppException {
        LocalDate inicio = console.lerData(Mensagens.get("oraculo.pedirInicio"));
        LocalDate fim = console.lerData(Mensagens.get("oraculo.pedirFim"));
        List<OraculoDTO> cotacoes = controller.listarPeriodo(inicio, fim);
        console.exibir(Mensagens.get("oraculo.periodo", inicio, fim));
        for (OraculoDTO cotacao : cotacoes) {
            exibirCotacao(cotacao);
        }
    }

    private void exibirCotacao(OraculoDTO cotacao) {
        console.exibir("  " + Formatador.data(cotacao.getData()) + "  " + Formatador.moeda(cotacao.getCotacao()));
    }
}
