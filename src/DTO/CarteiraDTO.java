package DTO;

public class CarteiraDTO {

    private int identificador;
    private String nomeTitular;
    private String corretora;

    public CarteiraDTO() {
    }

    public CarteiraDTO(int identificador, String nomeTitular, String corretora) {
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

    @Override
    public String toString() {
        return "CarteiraDTO{id=" + identificador
                + ", titular='" + nomeTitular + '\''
                + ", corretora='" + corretora + '\''
                + '}';
    }
}
