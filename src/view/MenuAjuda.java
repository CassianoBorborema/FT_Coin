package view;

import app.Main;
import view.opcoes_menus.*;
import java.util.Scanner;

public class MenuAjuda {
    private final Scanner input;

    public MenuAjuda(Scanner input) {
        this.input = input;
    }

    public void exibirMenuAjuda() {
        while (true) {
            System.out.println("\nQual opção deseja?");
            System.out.println("1. Como funciona");
            System.out.println("2. Créditos");
            System.out.println("0. Voltar");

            OpcoesMenuAjuda selecao = OpcoesMenuAjuda.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case TEXTO_AJUDA:
                	exibirTextoAjuda();
                    break;
                case CREDITOS:
                    exibirCreditos();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                	System.out.println("Opção inválida. Tente novamente.");
                    break;
            }
        }
    }
    
    private void exibirTextoAjuda() {
        System.out.println("\n========================================");
        System.out.println("           COMO FUNCIONA — FT_Coin      ");
        System.out.println("========================================");
        System.out.println();
        System.out.println("O FT_Coin é um sistema de apuração de ganhos e perdas");
        System.out.println("em carteiras de moedas virtuais.");
        System.out.println();
        System.out.println("---- CARTEIRA ----");
        System.out.println("Gerencie suas carteiras de criptomoedas.");
        System.out.println("  1. Incluir  : cadastra uma nova carteira informando");
        System.out.println("                nome do titular e corretora.");
        System.out.println("  2. Consultar: exibe os dados de uma carteira pelo ID.");
        System.out.println("  3. Editar   : atualiza titular ou corretora.");
        System.out.println("  4. Excluir  : remove a carteira (bloqueado se houver");
        System.out.println("                movimentações vinculadas).");
        System.out.println();
        System.out.println("---- MOVIMENTAÇÃO ----");
        System.out.println("Registre operações de compra e venda de moedas.");
        System.out.println("  1. Compra: informa o ID da carteira, a data e a");
        System.out.println("             quantidade comprada.");
        System.out.println("  2. Venda : informa o ID da carteira, a data e a");
        System.out.println("             quantidade vendida (não pode exceder o saldo).");
        System.out.println();
        System.out.println("  Datas devem ser informadas no formato dd/MM/yyyy.");
        System.out.println("  O sistema consulta o Oráculo para obter a cotação");
        System.out.println("  da moeda na data da operação.");
        System.out.println();
        System.out.println("---- RELATÓRIOS ----");
        System.out.println("Visualize informações consolidadas das carteiras.");
        System.out.println("  1. Carteiras por ID     : lista ordenada pelo identificador.");
        System.out.println("  2. Carteiras por titular: lista em ordem alfabética.");
        System.out.println("  3. Saldo atual          : quantidade de moedas disponível");
        System.out.println("                            em uma carteira.");
        System.out.println("  4. Histórico            : todas as movimentações de uma");
        System.out.println("                            carteira em ordem cronológica.");
        System.out.println("  5. Ganho/Perda          : resultado financeiro de cada");
        System.out.println("                            carteira em moeda real (R$),");
        System.out.println("                            calculado com as cotações do");
        System.out.println("                            Oráculo.");
        System.out.println();
        System.out.println("---- ORÁCULO ----");
        System.out.println("O Oráculo armazena a cotação diária da moeda virtual.");
        System.out.println("Sem cotação na data da operação, a movimentação não");
        System.out.println("pode ser registrada.");
        System.out.println();
        System.out.println("========================================");
    }

    private void exibirCreditos() {
        System.out.println("=== CRÉDITOS ===");
        System.out.println("Gabriel Rodrigues Firmino");
        System.out.println("Vinícius Pommer Petto");
        System.out.println("Vinícius Eduardo Machado");
        System.out.println("Vinícius Correia Leite");
        System.out.println("João Paulo Stradioto Pacolla");
        System.out.println("Nícolas Fogaça da Costa");
        System.out.println("Guilherme Leal Ribeiro");
        System.out.println("Cassiano Ladeia Borborema");
    }
}
