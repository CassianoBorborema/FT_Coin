package view;

import dto.CarteiraDTO;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Formatação de datas, valores e carteiras para exibição.
 */
public final class Formatador {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final double ARREDONDA_ZERO = 0.005;

    private Formatador() {
    }

    public static String data(LocalDate data) {
        return data.format(FORMATO_DATA);
    }

    public static String numero(double valor) {
        return String.format("%,.2f", zerarSeInsignificante(valor));
    }

    public static String moeda(double valor) {
        return "R$ " + numero(valor);
    }

    public static String quantidade(double quantidade) {
        return new DecimalFormat("#,##0.########").format(quantidade);
    }

    public static String resultado(double valor, double percentual) {
        return moeda(valor) + " (" + String.format("%.2f%%", zerarSeInsignificante(percentual)) + ")";
    }

    public static String carteira(CarteiraDTO carteira) {
        return "#" + carteira.getIdentificador() + " - " + carteira.getNomeTitular()
                + " (corretora: " + carteira.getCorretora() + ")";
    }

    /** Evita exibir "-0,00" para valores que arredondam para zero. */
    private static double zerarSeInsignificante(double valor) {
        return Math.abs(valor) < ARREDONDA_ZERO ? 0 : valor;
    }
}
