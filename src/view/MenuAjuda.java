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
            System.out.println("2. Creditos");
            System.out.println("0. Voltar");

            OpcoesMenuAjuda selecao = OpcoesMenuAjuda.opcao(Main.lerOpcao(input));

            switch (selecao) {
                case TEXTO_AJUDA:
                    exibirAjuda();
                    break;
                case CREDITOS:
                    exibirCreditos();
                    break;
                case VOLTAR:
                    return;
                case INVALIDA:
                    System.out.println("Opcao invalida. Tente novamente.");
                    break;
            }
        }
    }

    // -------------------------------------------------------------------------
    // Opcao 1 - Texto longo de ajuda
    // -------------------------------------------------------------------------
    private void exibirAjuda() {
        System.out.println();
        System.out.println("=========================================================");
        System.out.println(" FT_COIN - AJUDA DO SISTEMA");
        System.out.println("=========================================================");
        System.out.println();
        System.out.println("O FT_Coin e um sistema de linha de comando (CLI) destinado");
        System.out.println("ao gerenciamento de carteiras de moedas virtuais (FTC) e ao");
        System.out.println("acompanhamento de ganhos e perdas obtidos por meio de");
        System.out.println("operacoes de compra e venda.");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" MENU CARTEIRA");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("Permite gerenciar os dados cadastrais das carteiras.");
        System.out.println();
        System.out.println("1 - Incluir carteira");
        System.out.println("    Cadastra uma nova carteira informando:");
        System.out.println("    - Nome do titular");
        System.out.println("    - Corretora");
        System.out.println("    O identificador (ID) e gerado automaticamente pelo sistema.");
        System.out.println("    Guarde-o para consultas e movimentacoes futuras.");
        System.out.println();
        System.out.println("2 - Consultar carteira");
        System.out.println("    Exibe os dados de uma carteira a partir do seu ID.");
        System.out.println();
        System.out.println("3 - Editar carteira");
        System.out.println("    Permite alterar o nome do titular e a corretora");
        System.out.println("    de uma carteira existente, informando o seu ID.");
        System.out.println();
        System.out.println("4 - Excluir carteira");
        System.out.println("    Remove uma carteira do sistema informando o seu ID.");
        System.out.println("    O sistema exibira os dados e solicitara confirmacao");
        System.out.println("    (S/N) antes de concluir a exclusao.");
        System.out.println("    Observacao: nao e possivel excluir carteiras que");
        System.out.println("    possuam movimentacoes registradas.");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" MENU MOVIMENTACAO");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("Permite registrar operacoes de compra e venda de moedas");
        System.out.println("virtuais (FTC).");
        System.out.println();
        System.out.println("Dados solicitados em ambas as operacoes:");
        System.out.println("    - Identificador da carteira (ID)");
        System.out.println("    - Data da operacao (formato: dd/MM/yyyy)");
        System.out.println("    - Quantidade de FTC (deve ser maior que zero)");
        System.out.println();
        System.out.println("1 - Compra");
        System.out.println("    Registra uma aquisicao de moedas para uma carteira.");
        System.out.println();
        System.out.println("2 - Venda");
        System.out.println("    Registra a venda de moedas previamente adquiridas.");
        System.out.println();
        System.out.println("Validacoes realizadas:");
        System.out.println("    - Existencia da carteira informada.");
        System.out.println("    - Existencia de cotacao no Oraculo para a data informada.");
        System.out.println("    - Quantidade maior que zero.");
        System.out.println("    - Saldo suficiente em FTC para a venda.");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" MENU RELATORIOS");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("Disponibiliza consultas e analises das carteiras:");
        System.out.println();
        System.out.println("1 - Listar carteiras por identificador.");
        System.out.println("    Exibe todas as carteiras ordenadas pelo ID (crescente).");
        System.out.println();
        System.out.println("2 - Listar carteiras por nome do titular.");
        System.out.println("    Exibe todas as carteiras em ordem alfabetica.");
        System.out.println();
        System.out.println("3 - Exibir saldo atual de uma carteira.");
        System.out.println("    Mostra o saldo em FTC e o valor equivalente em BRL");
        System.out.println("    usando a cotacao de hoje cadastrada no Oraculo.");
        System.out.println("    Se nao houver cotacao para hoje, o valor em BRL");
        System.out.println("    sera indicado como indisponivel.");
        System.out.println();
        System.out.println("4 - Exibir historico de movimentacoes de uma carteira.");
        System.out.println("    Lista todas as operacoes registradas, ordenadas");
        System.out.println("    por data (crescente), com ID, tipo e quantidade.");
        System.out.println();
        System.out.println("5 - Exibir ganho ou perda total de cada carteira.");
        System.out.println("    Calcula o resultado financeiro de todas as carteiras");
        System.out.println("    cadastradas, ordenadas por ID. Para cada uma exibe:");
        System.out.println("    - Total investido: soma das compras x cotacao na data.");
        System.out.println("    - Total recuperado: soma das vendas x cotacao na data.");
        System.out.println("    - Saldo atual em FTC e valor atual em BRL.");
        System.out.println("    - Resultado = recuperado + valor atual - investido.");
        System.out.println("    Movimentacoes sem cotacao no Oraculo sao ignoradas");
        System.out.println("    com aviso ao usuario.");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" ORACULO DE COTACOES");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("As movimentacoes utilizam as cotacoes registradas no");
        System.out.println("Oraculo para calculo de saldo, ganhos e perdas.");
        System.out.println();
        System.out.println("Versao atual:");
        System.out.println("    - Apenas as cotacoes de hoje e ontem estao");
        System.out.println("      disponiveis no sistema.");
        System.out.println("    - Operacoes em outras datas resultarao na mensagem:");
        System.out.println("      \"Nao ha cotacao cadastrada para a data informada.\"");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" PERSISTENCIA");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("O sistema utiliza o padrao DAO para abstracao do acesso");
        System.out.println("a dados.");
        System.out.println();
        System.out.println("Implementacoes disponiveis:");
        System.out.println("    - Persistencia em memoria (padrao para demonstracao).");
        System.out.println();
        System.out.println("---------------------------------------------------------");
        System.out.println(" DICAS");
        System.out.println("---------------------------------------------------------");
        System.out.println();
        System.out.println("- Guarde o ID da carteira apos o cadastro, pois ele");
        System.out.println("  e necessario para todas as operacoes subsequentes.");
        System.out.println("- O formato de data aceito pelo sistema e dd/MM/yyyy.");
        System.out.println("- Utilize apenas datas com cotacao cadastrada no Oraculo.");
        System.out.println("- Verifique o saldo em FTC antes de registrar uma venda.");
        System.out.println("- Em caso de erro, leia atentamente a mensagem exibida.");
        System.out.println();
        System.out.println("=========================================================");
        System.out.println(" Fim da Ajuda");
        System.out.println("=========================================================");
        System.out.println();
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