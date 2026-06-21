package view;

import app.Main;
import java.util.Scanner;
import view.opcoes_menus.OpcoesMenuAjuda;

/**
 * Menu de Ajuda do sistema FT_Coin.
 * Exibe o texto de ajuda detalhado e a tela de creditos do sistema.
 */
public class MenuAjuda {

    private final Scanner input;

    public MenuAjuda(Scanner input) {
        this.input = input;
    }

    public void exibirMenuAjuda() {
        while (true) {
            System.out.println("\nQual opcao deseja?");
            System.out.println("1. Como funciona");
            System.out.println("0. Voltar");

            OpcoesMenuAjuda selecao = OpcoesMenuAjuda.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case TEXTO_AJUDA:
                    exibirTextoAjuda();
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
        System.out.println("\n=== COMO FUNCIONA O FT_COIN ===");
        System.out.println("O FT_Coin apura ganhos e perdas em uma carteira de moedas virtuais.");
        System.out.println("Navegue digitando o número da opção desejada e use 0 para voltar/sair.");
        System.out.println();
        System.out.println("1. Carteira: incluir, consultar, editar e excluir carteiras.");
        System.out.println("   A exclusão é bloqueada quando há movimentações vinculadas.");
        System.out.println("2. Movimentação: registrar compras e vendas de moeda virtual.");
        System.out.println("   A data informada precisa ter cotação cadastrada no Oráculo,");
        System.out.println("   e a venda exige saldo suficiente na carteira.");
        System.out.println("3. Oráculo: cadastrar, consultar e listar as cotações diárias,");
        System.out.println("   usadas para validar movimentações e calcular relatórios.");
        System.out.println("4. Relatórios: listar carteiras (por identificador ou titular),");
        System.out.println("   consultar saldo atual, histórico de movimentação e o");
        System.out.println("   ganho/perda total de cada carteira.");
        System.out.println("5. Ajuda: este guia de uso do sistema.");
        System.out.println();
        System.out.println("Datas devem ser informadas no formato dd/MM/yyyy.");
    }

    // -------------------------------------------------------------------------
    // Opcao 2 - Tela de creditos
    // -------------------------------------------------------------------------
    private void exibirCreditos() {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println(" FT_COIN");
        System.out.println(" Sistema de Apuracao de Ganhos e Perdas");
        System.out.println(" em Carteira de Moedas Virtuais");
        System.out.println("=========================================================");
        System.out.println();
        System.out.println(" Versao: 1.0.0");
        System.out.println(" Data  : Junho de 2026");
        System.out.println();
        System.out.println(" Desenvolvido para a disciplina:");
        System.out.println(" SI300 - Programacao Orientada a Objetos I");
        System.out.println();
        System.out.println(" Universidade Estadual de Campinas - UNICAMP");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" AUTORES");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("  Vinicius Pommer Petto      - RA 252160");
        System.out.println("  Vinicius Eduardo Machado   - RA 267014");
        System.out.println("  Vinicius Correia Leite     - RA 195444");
        System.out.println("  Nicolas Fogaca da Costa    - RA 241143");
        System.out.println("  Joao Paulo S. Pacolla      - RA 221170");
        System.out.println("  Gabriel Rodrigues Firmino  - RA 219400");
        System.out.println("  Guilherme Leal Ribeiro     - RA 237816");
        System.out.println("  Cassiano Ladeia Borborema  - RA 257721");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" TECNOLOGIAS");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("  - Java 8+");
        System.out.println("  - Arquitetura MVC");
        System.out.println("  - Padrao DAO");
        System.out.println("  - Interface CLI");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" COPYRIGHT");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("  (c) 2026 Equipe FT_Coin.");
        System.out.println();
        System.out.println("  Este software foi desenvolvido exclusivamente para fins");
        System.out.println("  academicos como projeto da disciplina SI300 -");
        System.out.println("  Programacao Orientada a Objetos I.");
        System.out.println();
        System.out.println("  Todos os direitos reservados aos autores.");
        System.out.println();
        System.out.println("=========================================================");
        System.out.println();
    }
}