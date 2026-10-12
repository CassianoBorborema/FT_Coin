package view;

import controller.CarteiraController;
import dto.CarteiraDTO;
import exception.AppException;

/**
 * Cadastro de carteiras: inclusão, consulta, edição e exclusão.
 */
public class MenuCarteira extends Menu {

    private final CarteiraController controller;

    public MenuCarteira(Console console, CarteiraController controller) {
        super(console, "Carteira");
        this.controller = controller;
        adicionarOpcao("Incluir carteira", this::incluirCarteira);
        adicionarOpcao("Consultar carteira", this::consultarCarteira);
        adicionarOpcao("Editar carteira", this::editarCarteira);
        adicionarOpcao("Excluir carteira", this::excluirCarteira);
    }

    private void incluirCarteira() throws AppException {
        String nomeTitular = console.lerLinha("Digite o nome do titular:");
        String corretora = console.lerLinha("Digite a corretora:");
        CarteiraDTO carteira = controller.incluir(nomeTitular, corretora);
        console.exibir("Carteira incluída com sucesso: " + Formatador.carteira(carteira));
    }

    private void consultarCarteira() throws AppException {
        int identificador = console.lerInteiro("Digite o identificador da carteira:");
        CarteiraDTO carteira = controller.consultar(identificador);
        console.exibir("Carteira encontrada: " + Formatador.carteira(carteira));
    }

    private void editarCarteira() throws AppException {
        int identificador = console.lerInteiro("Digite o identificador da carteira a editar:");
        CarteiraDTO atual = controller.consultar(identificador);
        console.exibir("Carteira atual: " + Formatador.carteira(atual));

        String nomeTitular = console.lerLinha("Digite o novo nome do titular:");
        String corretora = console.lerLinha("Digite a nova corretora:");
        CarteiraDTO atualizada = controller.editar(identificador, nomeTitular, corretora);
        console.exibir("Carteira atualizada com sucesso: " + Formatador.carteira(atualizada));
    }

    private void excluirCarteira() throws AppException {
        int identificador = console.lerInteiro("Digite o identificador da carteira a excluir:");
        CarteiraDTO carteira = controller.consultar(identificador);
        console.exibir("Carteira a excluir: " + Formatador.carteira(carteira));

        if (console.confirmar("Confirma exclusão?")) {
            controller.excluir(identificador);
            console.exibir("Carteira excluída com sucesso.");
        } else {
            console.exibir("Exclusão cancelada.");
        }
    }
}
