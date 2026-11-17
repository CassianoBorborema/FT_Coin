package model;

import dto.CarteiraDTO;
import exception.AppException;

/**
 * Carteira de moedas virtuais de um titular em uma corretora.
 */
public class Carteira {

    /** Tamanho das colunas {@code Titular} e {@code Corretora} da tabela {@code CARTEIRA}. */
    public static final int TAMANHO_MAXIMO = 50;

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
            throw new AppException("erro.carteira.semTitular");
        }
        if (nomeTitular.length() > TAMANHO_MAXIMO) {
            throw new AppException("erro.carteira.titularLongo", TAMANHO_MAXIMO);
        }
        if (corretora == null || corretora.trim().isEmpty()) {
            throw new AppException("erro.carteira.semCorretora");
        }
        if (corretora.length() > TAMANHO_MAXIMO) {
            throw new AppException("erro.carteira.corretoraLonga", TAMANHO_MAXIMO);
        }
    }

    public static Carteira fromDTO(CarteiraDTO dto) {
        return new Carteira(dto.getIdentificador(), dto.getNomeTitular(), dto.getCorretora());
    }

    public CarteiraDTO toDTO() {
        return new CarteiraDTO(identificador, nomeTitular, corretora);
    }
}
