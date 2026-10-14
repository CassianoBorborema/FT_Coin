package model;

import dto.OraculoDTO;
import exception.AppException;

/**
 * Cotação diária da moeda virtual informada pelo Oráculo.
 */
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
            throw new AppException("erro.cotacao.semData");
        }
        if (cotacao <= 0) {
            throw new AppException("erro.cotacao.naoPositiva");
        }
    }

    public static Oraculo fromDTO(OraculoDTO dto) {
        return new Oraculo(dto.getData(), dto.getCotacao());
    }

    public OraculoDTO toDTO() {
        return new OraculoDTO(data, cotacao);
    }
}
