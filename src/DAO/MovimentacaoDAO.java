package DAO;

import DTO.MovimentacaoDTO;
import exception.AppException;
import java.util.List;

public interface MovimentacaoDAO {

    void incluir(MovimentacaoDTO movimentacao) throws AppException;

    MovimentacaoDTO consultarPorId(int idMovimento) throws AppException;

    List<MovimentacaoDTO> listarPorCarteira(int idCarteira) throws AppException;

    double calcularSaldo(int idCarteira) throws AppException;

    boolean possuiMovimentacoes(int idCarteira) throws AppException;
}
