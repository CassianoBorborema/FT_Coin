package dao;

import config.Mensagens;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.TipoMovimentacao;

/**
 * Movimentações na tabela {@code movimentacao} do MariaDB.
 */
final class MovimentacaoDAOMariaDB implements MovimentacaoDAO {

    private static final String COLUNAS = "id, id_carteira, data, tipo, quantidade";

    private final FabricaConexao conexoes;

    MovimentacaoDAOMariaDB(FabricaConexao conexoes) {
        this.conexoes = conexoes;
    }

    /** Com identificador zero, o banco gera o identificador e ele é gravado de volta no DTO. */
    @Override
    public void incluir(MovimentacaoDTO movimentacao) throws AppException {
        int informado = movimentacao.getIdMovimento();
        if (informado != 0 && existeId(informado)) {
            throw new AppException("erro.movimentacao.duplicada", informado);
        }
        int id = conexoes.executar("banco.incluirMovimentacao", conexao -> {
            String sql = informado == 0
                    ? "INSERT INTO movimentacao (id_carteira, data, tipo, quantidade) VALUES (?, ?, ?, ?)"
                    : "INSERT INTO movimentacao (id_carteira, data, tipo, quantidade, id) VALUES (?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conexao.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, movimentacao.getIdCarteira());
                ps.setObject(2, movimentacao.getData());
                ps.setString(3, String.valueOf(movimentacao.getTipo().getCodigo()));
                ps.setDouble(4, movimentacao.getQuantidade());
                if (informado != 0) {
                    ps.setInt(5, informado);
                }
                ps.executeUpdate();
                return informado != 0 ? informado : chaveGerada(ps);
            }
        });
        movimentacao.setIdMovimento(id);
    }

    @Override
    public MovimentacaoDTO consultarPorId(int idMovimento) throws AppException {
        MovimentacaoDTO movimentacao = conexoes.executar("banco.consultarMovimentacao", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT " + COLUNAS + " FROM movimentacao WHERE id = ?")) {
                ps.setInt(1, idMovimento);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
        if (movimentacao == null) {
            throw new AppException("erro.movimentacao.naoEncontrada", idMovimento);
        }
        return movimentacao;
    }

    @Override
    public List<MovimentacaoDTO> listarPorCarteira(int idCarteira) throws AppException {
        return conexoes.executar("banco.listarMovimentacoes", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT " + COLUNAS + " FROM movimentacao WHERE id_carteira = ? ORDER BY data, id")) {
                ps.setInt(1, idCarteira);
                try (ResultSet rs = ps.executeQuery()) {
                    List<MovimentacaoDTO> lista = new ArrayList<>();
                    while (rs.next()) {
                        lista.add(mapear(rs));
                    }
                    return lista;
                }
            }
        });
    }

    @Override
    public boolean possuiMovimentacoes(int idCarteira) throws AppException {
        return conexoes.executar("banco.verificarMovimentacoes", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT 1 FROM movimentacao WHERE id_carteira = ? LIMIT 1")) {
                ps.setInt(1, idCarteira);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private boolean existeId(int idMovimento) throws AppException {
        return conexoes.executar("banco.verificarMovimentacao", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement("SELECT 1 FROM movimentacao WHERE id = ?")) {
                ps.setInt(1, idMovimento);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private static MovimentacaoDTO mapear(ResultSet rs) throws SQLException {
        return new MovimentacaoDTO(
                rs.getInt("id"),
                rs.getInt("id_carteira"),
                rs.getObject("data", LocalDate.class),
                tipo(rs.getString("tipo")),
                rs.getDouble("quantidade"));
    }

    private static TipoMovimentacao tipo(String codigo) throws SQLException {
        try {
            return TipoMovimentacao.fromChar(codigo.charAt(0));
        } catch (AppException e) {
            throw new SQLException(e.getMessage(), e);
        }
    }

    private static int chaveGerada(PreparedStatement ps) throws SQLException {
        try (ResultSet chaves = ps.getGeneratedKeys()) {
            if (!chaves.next()) {
                throw new SQLException(Mensagens.get("banco.semChaveGerada"));
            }
            return chaves.getInt(1);
        }
    }
}
