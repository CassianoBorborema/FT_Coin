package dao;

import dto.OraculoDTO;
import exception.AppException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import model.Oraculo;

final class OraculoDAOMariaDB implements OraculoDAO {

    private final FabricaConexao conexoes;

    OraculoDAOMariaDB(FabricaConexao conexoes) {
        this.conexoes = conexoes;
    }

    @Override
    public void incluir(OraculoDTO oraculo) throws AppException {
        Oraculo.fromDTO(oraculo).validar();
        if (existe(oraculo.getData())) {
            throw new AppException("Já existe cotação cadastrada para a data " + oraculo.getData() + ".");
        }
        conexoes.executar("incluir a cotação", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "INSERT INTO cotacao (data, valor) VALUES (?, ?)")) {
                ps.setObject(1, oraculo.getData());
                ps.setDouble(2, oraculo.getCotacao());
                return ps.executeUpdate();
            }
        });
    }

    @Override
    public OraculoDTO consultarPorData(LocalDate data) throws AppException {
        OraculoDTO cotacao = conexoes.executar("consultar a cotação", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement(
                    "SELECT data, valor FROM cotacao WHERE data = ?")) {
                ps.setObject(1, data);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? mapear(rs) : null;
                }
            }
        });
        if (cotacao == null) {
            throw new AppException("Não há cotação cadastrada para a data " + data + ".");
        }
        return cotacao;
    }

    @Override
    public List<OraculoDTO> listarTodas() throws AppException {
        return conexoes.executar("listar as cotações", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement("SELECT data, valor FROM cotacao ORDER BY data");
                 ResultSet rs = ps.executeQuery()) {
                List<OraculoDTO> lista = new ArrayList<>();
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
                return lista;
            }
        });
    }

    @Override
    public boolean existe(LocalDate data) throws AppException {
        return conexoes.executar("verificar a cotação", conexao -> {
            try (PreparedStatement ps = conexao.prepareStatement("SELECT 1 FROM cotacao WHERE data = ?")) {
                ps.setObject(1, data);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next();
                }
            }
        });
    }

    private static OraculoDTO mapear(ResultSet rs) throws SQLException {
        return new OraculoDTO(rs.getObject("data", LocalDate.class), rs.getDouble("valor"));
    }
}
