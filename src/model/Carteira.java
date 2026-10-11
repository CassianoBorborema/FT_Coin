package model;

import DTO.CarteiraDTO;
import exception.AppException;

public class Carteira {

    private int identificador;
    private String nomeTitular;
    private String corretora;

    public Carteira() {
    }

    public Carteira(int identificador, String nomeTitular, String corretora) {
        this.identificador = identificador;
        this.nomeTitular = nomeTitular;
        this.corretora = corretora;
    }

    public int getIdentificador() {
        return identificador;
    }

    public void setIdentificador(int identificador) {
        this.identificador = identificador;
    }

    public String getNomeTitular() {
        return nomeTitular;
    }

    public void setNomeTitular(String nomeTitular) {
        this.nomeTitular = nomeTitular;
    }

    public String getCorretora() {
        return corretora;
    }

    public void setCorretora(String corretora) {
        this.corretora = corretora;
    }

    public void validar() throws AppException {
        if (nomeTitular == null || nomeTitular.trim().isEmpty()) {
            throw new AppException("Nome do titular é obrigatório.");
        }
        if (corretora == null || corretora.trim().isEmpty()) {
            throw new AppException("Corretora é obrigatória.");
        }
    }

    public static Carteira fromDTO(CarteiraDTO dto) {
        return new Carteira(dto.getIdentificador(), dto.getNomeTitular(), dto.getCorretora());
    }

    public CarteiraDTO toDTO() {
        return new CarteiraDTO(identificador, nomeTitular, corretora);
    }
}
