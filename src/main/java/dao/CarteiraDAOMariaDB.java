package dao;

import config.Mensagens;
import dto.CarteiraDTO;
import exception.AppException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Carteiras na tabela {@code carteira} do MariaDB.
 */
final class CarteiraDAOMariaDB implements CarteiraDAO {

    private static final String COLUNAS = "id, nome_titular, corretora";

    private final FabricaConexao conexoes;

    CarteiraDAOMariaDB(FabricaConexao conexoes) {
        this.conexoes = conexoes;
    }

    /** Com identificador zero, o banco gera o identificador e ele é gravado de volta no DTO. */
    @Override
    public void incluir(CarteiraDTO carteira) throws AppException {
        int informado = carteira.getIdentificador();
        if (informado != 0 && existe(informado)) {
            throw new AppException("erro.carteira.duplicada", informado);
        }
        int id = conexoes.executar("banco.incluirCarteira", conexao -> {
            String sql = informado == 0
                    ? "INSERT INTO carteira (nome_titular, corretora) VALUES (?, ?)"
                    : "INSERT INTO carteira (nome_titular, corretora, id) VALUES (?, ?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, carteira.getNomeTitular());
                ps.setString(2, carteira.getCorretora());
                if (informado != 0) {
                    ps.setInt(3, informado);
                }
                ps.executeUpdate();
                return informado != 0 ? informado : chaveGerada(ps);
            }
        });
        carteira.setIdentificador(id);
    }

    @Override
    public CarteiraDTO consultarPorId(int identificador) throws AppException {
        CarteiraDTO carteira = conexoes.executar("banco.consultarCarteira", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT " + COLUNAS + " FROM carteira WHERE id = ?")) {
                ps.setInt(1, identificador);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
        if (carteira == null) {
            throw naoEncontrada(identificador);
        }
        return carteira;
    }

    @Override
    public List<CarteiraDTO> listarTodas() throws AppException {
        return conexoes.executar("banco.listarCarteiras", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT " + COLUNAS + " FROM carteira ORDER BY id");
                 ResultSet rs = ps.executeQuery()) {
                List<CarteiraDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        });
    }

    @Override
    public void atualizar(CarteiraDTO carteira) throws AppException {
        int alteradas = conexoes.executar("banco.atualizarCarteira", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "UPDATE carteira SET nome_titular = ?, corretora = ? WHERE id = ?")) {
                ps.setString(1, carteira.getNomeTitular());
                ps.setString(2, carteira.getCorretora());
                ps.setInt(3, carteira.getIdentificador());
                return ps.executeUpdate();
            }
        });
        if (alteradas == 0) {
            throw naoEncontrada(carteira.getIdentificador());
        }
    }

    @Override
    public void excluir(int identificador) throws AppException {
        int excluidas = conexoes.executar("banco.excluirCarteira", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement("DELETE FROM carteira WHERE id = ?")) {
                ps.setInt(1, identificador);
                return ps.executeUpdate();
            }
        });
        if (excluidas == 0) {
            throw naoEncontrada(identificador);
        }
    }

    @Override
    public boolean existe(int identificador) throws AppException {
        return conexoes.executar("banco.verificarCarteira", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement("SELECT 1 FROM carteira WHERE id = ?")) {
                ps.setInt(1, identificador);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private static CarteiraDTO mapear(ResultSet rs) throws SQLException {
        return new CarteiraDTO(rs.getInt("id"), rs.getString("nome_titular"), rs.getString("corretora"));
    }

    private static int chaveGerada(PreparedStatement ps) throws SQLException {
        try (ResultSet chaves = ps.getGeneratedKeys()) {
            if (!chaves.next()) {
                throw new SQLException(Mensagens.get("banco.semChaveGerada"));
            }
            return chaves.getInt(1);
        }
    }

    private static AppException naoEncontrada(int identificador) {
        return new AppException("erro.carteira.naoEncontrada", identificador);
    }
}
