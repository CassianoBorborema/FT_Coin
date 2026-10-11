package model;

import DTO.MovimentacaoDTO;
import exception.AppException;

public class Movimentacao {

    private int idMovimento;
    private int idCarteira;
    private java.time.LocalDate data;
    private TipoMovimentacao tipo;
    private double quantidade;

    public Movimentacao() {
    }

    public Movimentacao(int idMovimento, int idCarteira, java.time.LocalDate data, TipoMovimentacao tipo, double quantidade) {
        this.idMovimento = idMovimento;
        this.idCarteira = idCarteira;
        this.data = data;
        this.tipo = tipo;
        this.quantidade = quantidade;
    }

    public int getIdMovimento() {
        return idMovimento;
    }

    public void setIdMovimento(int idMovimento) {
        this.idMovimento = idMovimento;
    }

    public int getIdCarteira() {
        return idCarteira;
    }

    public void setIdCarteira(int idCarteira) {
        this.idCarteira = idCarteira;
    }

    public java.time.LocalDate getData() {
        return data;
    }

    public void setData(java.time.LocalDate data) {
        this.data = data;
    }

    public TipoMovimentacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimentacao tipo) {
        this.tipo = tipo;
    }

    public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public void validar() throws AppException {
        if (idCarteira <= 0) {
            throw new AppException("Identificador da carteira deve ser maior que zero.");
        }
        if (data == null) {
            throw new AppException("Data da movimentação é obrigatória.");
        }
        if (tipo == null) {
            throw new AppException("Tipo da movimentação é obrigatório.");
        }
        if (quantidade <= 0) {
            throw new AppException("Quantidade deve ser maior que zero.");
        }
    }

    public static Movimentacao fromDTO(MovimentacaoDTO dto) {
        return new Movimentacao(
                dto.getIdMovimento(),
                dto.getIdCarteira(),
                dto.getData(),
                dto.getTipo(),
                dto.getQuantidade()
        );
    }

    public MovimentacaoDTO toDTO() {
        return new MovimentacaoDTO(idMovimento, idCarteira, data, tipo, quantidade);
    }
}
