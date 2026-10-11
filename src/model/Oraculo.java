package model;

import DTO.OraculoDTO;
import exception.AppException;

public class Oraculo {

    private java.time.LocalDate data;
    private double cotacao;

    public Oraculo() {
    }

    public Oraculo(java.time.LocalDate data, double cotacao) {
        this.data = data;
        this.cotacao = cotacao;
    }

    public java.time.LocalDate getData() {
        return data;
    }

    public void setData(java.time.LocalDate data) {
        this.data = data;
    }

    public double getCotacao() {
        return cotacao;
    }

    public void setCotacao(double cotacao) {
        this.cotacao = cotacao;
    }

    public void validar() throws AppException {
        if (data == null) {
            throw new AppException("Data da cotação é obrigatória.");
        }
        if (cotacao <= 0) {
            throw new AppException("Cotação deve ser maior que zero.");
        }
    }

    public static Oraculo fromDTO(OraculoDTO dto) {
        return new Oraculo(dto.getData(), dto.getCotacao());
    }

    public OraculoDTO toDTO() {
        return new OraculoDTO(data, cotacao);
    }
}
