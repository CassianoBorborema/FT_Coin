package dao;

import config.Configuracao;
import config.Mensagens;
import exception.AppException;

/**
 * DAOs de uma forma de persistência. Controllers e views dependem só das interfaces, então trocar
 * a persistência não altera o resto da aplicação.
 *
 * @param descricao texto exibido ao usuário, no idioma escolhido
 */
public record Persistencia(CarteiraDAO carteiras, MovimentacaoDAO movimentacoes, OraculoDAO cotacoes,
                           String descricao) {

    public static Persistencia emMemoria() {
        return new Persistencia(new CarteiraDAOMemoria(), new MovimentacaoDAOMemoria(), new OraculoDAOMemoria(),
                Mensagens.get("persistencia.memoria"));
    }

    /**
     * Conecta ao MariaDB das chaves {@code db.*} e cria as tabelas que faltarem.
     *
     * @throws AppException se a configuração estiver incompleta ou o banco estiver inacessível
     */
    public static Persistencia mariaDB(Configuracao configuracao) throws AppException {
        return mariaDB(new FabricaConexao(FabricaConexao.ConfiguracaoBanco.carregar(configuracao)));
    }

    static Persistencia mariaDB(FabricaConexao conexoes) throws AppException {
        conexoes.criarTabelas();
        return new Persistencia(new CarteiraDAOMariaDB(conexoes), new MovimentacaoDAOMariaDB(conexoes),
                new OraculoDAOMariaDB(conexoes), Mensagens.get("persistencia.mariadb", conexoes.configuracao().url()));
    }
}
