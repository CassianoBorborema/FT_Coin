package DTO;

import model.TipoMovimentacao;
import java.time.LocalDate;

public class MovimentacaoDTO {

    private int idMovimento;
    private int idCarteira;
    private LocalDate data;
    private TipoMovimentacao tipo;
    private double quantidade;

    public MovimentacaoDTO() {
    }

    public MovimentacaoDTO(int idMovimento, int idCarteira, LocalDate data, TipoMovimentacao tipo, double quantidade) {
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

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
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

    @Override
    public String toString() {
        return "MovimentacaoDTO{idMovimento=" + idMovimento
                + ", idCarteira=" + idCarteira
                + ", data=" + data
                + ", tipo=" + (tipo != null ? tipo.getCodigo() : '?')
                + ", quantidade=" + quantidade
                + '}';
    }
}
