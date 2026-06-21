package view;

import app.Main;
import controller.OraculoController;
import DTO.OraculoDTO;
import exception.AppException;
import view.opcoes_menus.OpcoesMenuOraculo;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MenuOraculo {

    private final Scanner input;
    private final OraculoController controller;

    public MenuOraculo(Scanner input, OraculoController controller) {
        this.input = input;
        this.controller = controller;
    }

    public void exibirMenuOraculo() {
        while (true) {
            System.out.println("\nQual opção deseja?");
            System.out.println("1. Cadastrar cotação");
            System.out.println("2. Consultar cotação por data");
            System.out.println("3. Listar cotações");
            System.out.println("0. Voltar");

            OpcoesMenuOraculo selecao = OpcoesMenuOraculo.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case CADASTRAR:
                    cadastrarCotacao();
                    break;
                case CONSULTAR:
                    consultarCotacao();
                    break;
                case LISTAR:
                    listarCotacoes();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }

    private void cadastrarCotacao() {
        try {
            LocalDate data = Main.lerData(input, "Digite a data da cotação");
            double cotacao = Main.lerDouble(input, "Digite o valor da cotação:");
            OraculoDTO cadastrada = controller.cadastrar(data, cotacao);
            System.out.println("Cotação cadastrada com sucesso:");
            System.out.println(cadastrada);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void consultarCotacao() {
        try {
            LocalDate data = Main.lerData(input, "Digite a data da cotação");
            OraculoDTO cotacao = controller.consultar(data);
            System.out.println("Cotação encontrada:");
            System.out.println(cotacao);
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }

    private void listarCotacoes() {
        try {
            List<OraculoDTO> cotacoes = controller.listarTodas();
            if (cotacoes.isEmpty()) {
                System.out.println("Nenhuma cotação cadastrada.");
                return;
            }
            System.out.println("Cotações cadastradas:");
            for (OraculoDTO cotacao : cotacoes) {
                System.out.println(cotacao);
            }
        } catch (AppException e) {
            System.out.println("Erro: " + e.getMessage());
        }
    }
}
