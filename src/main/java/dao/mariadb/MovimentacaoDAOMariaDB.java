package dao.mariadb;

import dao.MovimentacaoDAO;
import dto.MovimentacaoDTO;
import exception.AppException;
import infra.db.FabricaConexao;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.TipoMovimentacao;

public class MovimentacaoDAOMariaDB implements MovimentacaoDAO {

    private static final String COLUNAS = "id, id_carteira, data, tipo, quantidade";

    private final FabricaConexao conexoes;

    public MovimentacaoDAOMariaDB(FabricaConexao conexoes) {
        this.conexoes = conexoes;
    }

    /** Com identificador zero, o banco gera o identificador e ele é gravado de volta no DTO. */
    @Override
    public void incluir(MovimentacaoDTO movimentacao) throws AppException {
        int informado = movimentacao.getIdMovimento();
        if (informado != 0 && existeId(informado)) {
            throw new AppException("Já existe uma movimentação com o identificador " + informado + ".");
        }
        int id = conexoes.executar("incluir a movimentação", conexao -> {
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
        MovimentacaoDTO movimentacao = conexoes.executar("consultar a movimentação", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT " + COLUNAS + " FROM movimentacao WHERE id = ?")) {
                ps.setInt(1, idMovimento);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
        if (movimentacao == null) {
            throw new AppException("Movimentação com identificador " + idMovimento + " não encontrada.");
        }
        return movimentacao;
    }

    @Override
    public List<MovimentacaoDTO> listarPorCarteira(int idCarteira) throws AppException {
        return conexoes.executar("listar as movimentações", conexao -> {
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
    public double calcularSaldo(int idCarteira) throws AppException {
        return conexoes.executar("calcular o saldo", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT COALESCE(SUM(CASE tipo WHEN 'C' THEN quantidade ELSE -quantidade END), 0)"
                            + " FROM movimentacao WHERE id_carteira = ?")) {
                ps.setInt(1, idCarteira);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getDouble(1);
                }
            }
        });
    }

    @Override
    public boolean possuiMovimentacoes(int idCarteira) throws AppException {
        return conexoes.executar("verificar as movimentações", conexao -> {
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
        return conexoes.executar("verificar a movimentação", conexao -> {
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
                throw new SQLException("o banco não informou o identificador gerado");
            }
            return chaves.getInt(1);
        }
    }
}
