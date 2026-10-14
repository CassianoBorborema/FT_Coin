package app;

import config.Configuracao;
import controller.CarteiraController;
import controller.MovimentacaoController;
import controller.OraculoController;
import controller.RelatorioController;
import dao.CarteiraDAO;
import dao.FabricaDAO;
import dao.MovimentacaoDAO;
import dao.mariadb.FabricaDAOMariaDB;
import dao.memoria.FabricaDAOMemoria;
import exception.AppException;
import infra.db.ConfiguracaoBanco;
import infra.db.FabricaConexao;
import infra.db.InicializadorBanco;
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
        } catch (NoSuchElementException e) {
            console.exibir("\nEntrada encerrada. Saindo da aplicação...");
        }
    }

    private static MenuPrincipal montarAplicacao(Console console) throws AppException {
        Configuracao configuracao = Configuracao.carregar();
        Clock relogio = Clock.systemDefaultZone();

        FabricaDAO fabricaDAO = criarFabricaDAO(configuracao, console);
        CarteiraDAO carteiraDAO = fabricaDAO.carteiraDAO();
        MovimentacaoDAO movimentacaoDAO = fabricaDAO.movimentacaoDAO();

        ProvedorCotacao cotacoes = new CotacaoComHistorico(
                new OraculoSimulado(ParametrosOraculoSimulado.carregar(configuracao), relogio),
                fabricaDAO.oraculoDAO());

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

    /**
     * Com {@code persistencia=auto}, tenta o MariaDB e, se não conseguir conectar, avisa o motivo
     * e segue em memória. Com {@code memoria}, nem tenta o banco.
     */
    private static FabricaDAO criarFabricaDAO(Configuracao configuracao, Console console) throws AppException {
        String persistencia = configuracao.getTexto("persistencia", "auto");
        switch (persistencia) {
            case "auto":
                try {
                    ConfiguracaoBanco banco = ConfiguracaoBanco.carregar(configuracao);
                    FabricaConexao conexoes = new FabricaConexao(banco);
                    new InicializadorBanco(conexoes).inicializar();
                    console.exibir("Persistência: MariaDB (" + banco.url() + ").");
                    return new FabricaDAOMariaDB(conexoes);
                } catch (AppException e) {
                    console.exibir("Aviso: MariaDB indisponível. " + e.getMessage());
                    return emMemoria(console);
                }
            case "memoria":
                return emMemoria(console);
            default:
                throw new AppException("Persistência desconhecida: " + persistencia + ". Use auto ou memoria.");
        }
    }

    private static FabricaDAO emMemoria(Console console) {
        console.exibir("Persistência: memória (os dados são perdidos ao sair).");
        return new FabricaDAOMemoria();
    }

    private static void exibirBoasVindas(Console console) {
        console.exibir("****************************");
        console.exibir("====Bem vindo ao FT_Coin====");
        console.exibir("****************************");
    }
}
