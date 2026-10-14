package dao;

import dto.MovimentacaoDTO;
import exception.AppException;
import java.util.List;

public sealed interface MovimentacaoDAO permits MovimentacaoDAOMemoria, MovimentacaoDAOMariaDB {

    void incluir(MovimentacaoDTO movimentacao) throws AppException;

    MovimentacaoDTO consultarPorId(int idMovimento) throws AppException;

    List<MovimentacaoDTO> listarPorCarteira(int idCarteira) throws AppException;

    double calcularSaldo(int idCarteira) throws AppException;

    boolean possuiMovimentacoes(int idCarteira) throws AppException;
}
