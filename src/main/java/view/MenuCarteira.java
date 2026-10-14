package view;

import config.Mensagens;
import controller.CarteiraController;
import dto.CarteiraDTO;
import exception.AppException;

/**
 * Cadastro de carteiras: inclusão, consulta, edição e exclusão.
 */
public class MenuCarteira extends Menu {

    private final CarteiraController controller;

    public MenuCarteira(Console console, CarteiraController controller) {
        super(console, Mensagens.get("carteira.titulo"));
        this.controller = controller;
        adicionarOpcao(Mensagens.get("carteira.incluir"), this::incluirCarteira);
        adicionarOpcao(Mensagens.get("carteira.consultar"), this::consultarCarteira);
        adicionarOpcao(Mensagens.get("carteira.editar"), this::editarCarteira);
        adicionarOpcao(Mensagens.get("carteira.excluir"), this::excluirCarteira);
    }

    private void incluirCarteira() throws AppException {
        String nomeTitular = console.lerLinha(Mensagens.get("carteira.pedirTitular"));
        String corretora = console.lerLinha(Mensagens.get("carteira.pedirCorretora"));
        CarteiraDTO carteira = controller.incluir(nomeTitular, corretora);
        console.exibir(Mensagens.get("carteira.incluida", Formatador.carteira(carteira)));
    }

    private void consultarCarteira() throws AppException {
        int identificador = console.lerInteiro(Mensagens.get("pedir.carteira"));
        CarteiraDTO carteira = controller.consultar(identificador);
        console.exibir(Mensagens.get("carteira.encontrada", Formatador.carteira(carteira)));
    }

    private void editarCarteira() throws AppException {
        int identificador = console.lerInteiro(Mensagens.get("carteira.pedirEditar"));
        CarteiraDTO atual = controller.consultar(identificador);
        console.exibir(Mensagens.get("carteira.atual", Formatador.carteira(atual)));

        String nomeTitular = console.lerLinha(Mensagens.get("carteira.pedirNovoTitular"));
        String corretora = console.lerLinha(Mensagens.get("carteira.pedirNovaCorretora"));
        CarteiraDTO atualizada = controller.editar(identificador, nomeTitular, corretora);
        console.exibir(Mensagens.get("carteira.atualizada", Formatador.carteira(atualizada)));
    }

    private void excluirCarteira() throws AppException {
        int identificador = console.lerInteiro(Mensagens.get("carteira.pedirExcluir"));
        CarteiraDTO carteira = controller.consultar(identificador);
        console.exibir(Mensagens.get("carteira.aExcluir", Formatador.carteira(carteira)));

        if (console.confirmar(Mensagens.get("carteira.confirmarExclusao"))) {
            controller.excluir(identificador);
            console.exibir(Mensagens.get("carteira.excluida"));
        } else {
            console.exibir(Mensagens.get("carteira.exclusaoCancelada"));
        }
    }
}
