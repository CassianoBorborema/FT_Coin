package DAO.memoria;

import DAO.MovimentacaoDAO;
import DTO.MovimentacaoDTO;
import exception.AppException;
import model.TipoMovimentacao;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MovimentacaoDAOMemoria implements MovimentacaoDAO {

    private final Map<Integer, MovimentacaoDTO> movimentacoes = new HashMap<>();
    private int proximoId = 1;

    @Override
    public void incluir(MovimentacaoDTO movimentacao) throws AppException {
        int id = movimentacao.getIdMovimento();

        if (id == 0) {
            id = proximoId++;
        } else if (movimentacoes.containsKey(id)) {
            throw new AppException("Já existe uma movimentação com o identificador " + id + ".");
        } else if (id >= proximoId) {
            proximoId = id + 1;
        }

        MovimentacaoDTO copia = copiar(movimentacao);
        copia.setIdMovimento(id);
        movimentacoes.put(id, copia);
        movimentacao.setIdMovimento(id);
    }

    @Override
    public MovimentacaoDTO consultarPorId(int idMovimento) throws AppException {
        MovimentacaoDTO movimentacao = movimentacoes.get(idMovimento);
        if (movimentacao == null) {
            throw new AppException("Movimentação com identificador " + idMovimento + " não encontrada.");
        }
        return copiar(movimentacao);
    }

    @Override
    public List<MovimentacaoDTO> listarPorCarteira(int idCarteira) throws AppException {
        List<MovimentacaoDTO> lista = new ArrayList<>();
        for (MovimentacaoDTO movimentacao : movimentacoes.values()) {
            if (movimentacao.getIdCarteira() == idCarteira) {
                lista.add(copiar(movimentacao));
            }
        }
        return lista;
    }

    @Override
    public double calcularSaldo(int idCarteira) throws AppException {
        double saldo = 0;
        for (MovimentacaoDTO movimentacao : movimentacoes.values()) {
            if (movimentacao.getIdCarteira() != idCarteira) {
                continue;
            }
            if (movimentacao.getTipo() == TipoMovimentacao.COMPRA) {
                saldo += movimentacao.getQuantidade();
            } else if (movimentacao.getTipo() == TipoMovimentacao.VENDA) {
                saldo -= movimentacao.getQuantidade();
            }
        }
        return saldo;
    }

    @Override
    public boolean possuiMovimentacoes(int idCarteira) throws AppException {
        for (MovimentacaoDTO movimentacao : movimentacoes.values()) {
            if (movimentacao.getIdCarteira() == idCarteira) {
                return true;
            }
        }
        return false;
    }

    private MovimentacaoDTO copiar(MovimentacaoDTO original) {
        return new MovimentacaoDTO(
                original.getIdMovimento(),
                original.getIdCarteira(),
                original.getData(),
                original.getTipo(),
                original.getQuantidade()
        );
    }
}
