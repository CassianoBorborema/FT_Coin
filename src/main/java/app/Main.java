package app;

import config.Configuracao;
import controller.CarteiraController;
import controller.MovimentacaoController;
import controller.OraculoController;
import controller.RelatorioController;
import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.OraculoDAO;
import dao.memoria.CarteiraDAOMemoria;
import dao.memoria.MovimentacaoDAOMemoria;
import dao.memoria.OraculoDAOMemoria;
import exception.AppException;
import java.time.Clock;
import java.util.NoSuchElementException;
import model.ProvedorCotacao;
import oraculo.CotacaoComHistorico;
import oraculo.OraculoSimulado;
import oraculo.ParametrosOraculoSimulado;
import view.Console;
import view.MenuAjuda;
import view.MenuCarteira;
import view.MenuMovimentacao;
import view.MenuOraculo;
import view.MenuPrincipal;
import view.MenuRelatorios;

/**
 * Ponto de entrada do FT_Coin: monta as dependências da aplicação e abre o menu principal.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Console console = new Console(System.in, System.out);
        try {
            MenuPrincipal menu = montarAplicacao(console);
            exibirBoasVindas(console);
            menu.exibir();
            console.exibir("Saindo da aplicação...");
        } catch (AppException e) {
            console.exibirErro("Não foi possível iniciar o FT_Coin. " + e.getMessage());
        } catch (NoSuchElementException e) {
            console.exibir("\nEntrada encerrada. Saindo da aplicação...");
        }
    }

    private static MenuPrincipal montarAplicacao(Console console) throws AppException {
        Configuracao configuracao = Configuracao.carregar();
        Clock relogio = Clock.systemDefaultZone();

        CarteiraDAO carteiraDAO = new CarteiraDAOMemoria();
        MovimentacaoDAO movimentacaoDAO = new MovimentacaoDAOMemoria();
        OraculoDAO oraculoDAO = new OraculoDAOMemoria();

        ProvedorCotacao cotacoes = new CotacaoComHistorico(
                new OraculoSimulado(ParametrosOraculoSimulado.carregar(configuracao), relogio), oraculoDAO);

        CarteiraController carteiraController = new CarteiraController(carteiraDAO, movimentacaoDAO);
        MovimentacaoController movimentacaoController =
                new MovimentacaoController(movimentacaoDAO, carteiraDAO, cotacoes, relogio);
        OraculoController oraculoController = new OraculoController(cotacoes, relogio);
        RelatorioController relatorioController =
                new RelatorioController(carteiraDAO, movimentacaoDAO, cotacoes, relogio);

        return new MenuPrincipal(console,
                new MenuCarteira(console, carteiraController),
                new MenuMovimentacao(console, movimentacaoController),
                new MenuOraculo(console, oraculoController),
                new MenuRelatorios(console, relatorioController),
                new MenuAjuda(console));
    }

    private static void exibirBoasVindas(Console console) {
        console.exibir("****************************");
        console.exibir("====Bem vindo ao FT_Coin====");
        console.exibir("****************************");
    }
}
