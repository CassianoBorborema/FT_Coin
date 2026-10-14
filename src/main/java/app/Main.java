package app;

import config.Configuracao;
import controller.CarteiraController;
import controller.MovimentacaoController;
import controller.OraculoController;
import controller.RelatorioController;
import dao.Persistencia;
import exception.AppException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import model.ProvedorCotacao;
import oraculo.CotacaoComHistorico;
import oraculo.OraculoSimulado;
import view.Console;
import view.MenuAjuda;
import view.MenuCarteira;
import view.MenuMovimentacao;
import view.MenuOraculo;
import view.MenuPrincipal;
import view.MenuRelatorios;

/**
 * Ponto de entrada: monta as dependências e abre o menu principal.
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
        } catch (NoSuchElementException _) {
            console.exibir("\nEntrada encerrada. Saindo da aplicação...");
        }
    }

    private static MenuPrincipal montarAplicacao(Console console) throws AppException {
        Configuracao configuracao = Configuracao.carregar();
        Clock relogio = Clock.systemDefaultZone();

        LocalDate vigenciaCredito = configuracao.getData("credito.vigencia");
        Persistencia persistencia = escolherPersistencia(configuracao, console);
        console.exibir("Persistência: " + persistencia.descricao() + ".");

        ProvedorCotacao cotacoes = new CotacaoComHistorico(
                new OraculoSimulado(OraculoSimulado.Parametros.carregar(configuracao), relogio),
                persistencia.cotacoes());

        CarteiraController carteiraController =
                new CarteiraController(persistencia.carteiras(), persistencia.movimentacoes());
        MovimentacaoController movimentacaoController = new MovimentacaoController(
                persistencia.movimentacoes(), persistencia.carteiras(), cotacoes, relogio, vigenciaCredito);
        OraculoController oraculoController = new OraculoController(cotacoes, relogio);
        RelatorioController relatorioController = new RelatorioController(
                persistencia.carteiras(), persistencia.movimentacoes(), cotacoes, relogio, vigenciaCredito);

        return new MenuPrincipal(console,
                new MenuCarteira(console, carteiraController),
                new MenuMovimentacao(console, movimentacaoController),
                new MenuOraculo(console, oraculoController),
                new MenuRelatorios(console, relatorioController),
                new MenuAjuda(console));
    }

    /**
     * Com {@code persistencia=auto}, tenta o MariaDB e, se não conseguir conectar, avisa o motivo
     * e segue em memória. Com {@code memoria}, nem tenta o banco.
     */
    private static Persistencia escolherPersistencia(Configuracao configuracao, Console console)
            throws AppException {
        String persistencia = configuracao.getTexto("persistencia", "auto");
        return switch (persistencia) {
            case "auto" -> {
                try {
                    yield Persistencia.mariaDB(configuracao);
                } catch (AppException e) {
                    console.exibir("Aviso: MariaDB indisponível. " + e.getMessage());
                    yield Persistencia.emMemoria();
                }
            }
            case "memoria" -> Persistencia.emMemoria();
            default -> throw new AppException("Persistência desconhecida: " + persistencia + ". Use auto ou memoria.");
        };
    }

    private static void exibirBoasVindas(Console console) {
        console.exibir("****************************");
        console.exibir("====Bem vindo ao FT_Coin====");
        console.exibir("****************************");
    }
}
