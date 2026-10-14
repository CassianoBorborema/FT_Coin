package app;

import config.Configuracao;
import config.Mensagens;
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
 * Ponto de entrada: escolhe o idioma, monta as dependências e abre o menu principal.
 */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        Console console = Console.doTerminal();
        try {
            Configuracao configuracao = Configuracao.carregar();
            escolherIdioma(configuracao, console);
            MenuPrincipal menu = montarAplicacao(configuracao, console);
            console.exibir(Mensagens.get("app.boasVindas"));
            menu.exibir();
            console.exibir(Mensagens.get("app.saindo"));
        } catch (AppException e) {
            console.exibirErro(Mensagens.get("app.falhaInicio", e));
        } catch (NoSuchElementException _) {
            console.exibir(Mensagens.get("app.entradaEncerrada"));
        }
    }

    /** Usa a chave {@code idioma} da configuração ou, sem ela, pergunta até receber uma opção válida. */
    private static void escolherIdioma(Configuracao configuracao, Console console) throws AppException {
        String configurado = configuracao.getTexto("idioma", "");
        if (!configurado.isBlank()) {
            Mensagens.definirIdioma(configurado);
            return;
        }
        while (true) {
            console.exibir("Idioma / Language / Idioma:");
            console.exibir("1. Português");
            console.exibir("2. English");
            console.exibir("3. Español");
            int opcao = console.lerOpcao();
            if (opcao >= 1 && opcao <= Mensagens.IDIOMAS.size()) {
                Mensagens.definirIdioma(Mensagens.IDIOMAS.get(opcao - 1));
                return;
            }
        }
    }

    private static MenuPrincipal montarAplicacao(Configuracao configuracao, Console console) throws AppException {
        Clock relogio = Clock.systemDefaultZone();

        LocalDate vigenciaCredito = configuracao.getData("credito.vigencia");
        Persistencia persistencia = escolherPersistencia(configuracao, console);
        console.exibir(Mensagens.get("app.persistencia", persistencia.descricao()));

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
                    console.exibir(Mensagens.get("app.mariaDBIndisponivel", e));
                    yield Persistencia.emMemoria();
                }
            }
            case "memoria" -> Persistencia.emMemoria();
            default -> throw new AppException("erro.persistencia", persistencia);
        };
    }
}
