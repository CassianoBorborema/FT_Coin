package DTO;

import java.time.LocalDate;

public class OraculoDTO {

    private LocalDate data;
    private double cotacao;

    public OraculoDTO() {
    }

    public OraculoDTO(LocalDate data, double cotacao) {
        this.data = data;
        this.cotacao = cotacao;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public double getCotacao() {
        return cotacao;
    }

    public void setCotacao(double cotacao) {
        this.cotacao = cotacao;
    }

    @Override
    public String toString() {
        return "OraculoDTO{data=" + data + ", cotacao=" + cotacao + '}';
    }
}
