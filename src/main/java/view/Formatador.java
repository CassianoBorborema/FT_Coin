package view;

import config.Mensagens;
import dto.CarteiraDTO;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;

/**
 * Formatação de datas, valores e carteiras para exibição, nos padrões do idioma escolhido.
 * Os valores são sempre em reais, qualquer que seja o idioma.
 */
public final class Formatador {

    private static final double ARREDONDA_ZERO = 0.005;

    private Formatador() {
    }

    public static String data(LocalDate data) {
        return Mensagens.data(data);
    }

    public static String numero(double valor) {
        return String.format(Mensagens.locale(), "%,.2f", zerarSeInsignificante(valor));
    }

    public static String moeda(double valor) {
        return "R$ " + numero(valor);
    }

    public static String quantidade(double quantidade) {
        return new DecimalFormat("#,##0.########", DecimalFormatSymbols.getInstance(Mensagens.locale()))
                .format(quantidade);
    }

    public static String resultado(double valor, double percentual) {
        return moeda(valor) + " (" + String.format(Mensagens.locale(), "%.2f%%", zerarSeInsignificante(percentual)) + ")";
    }

    /** Titular e corretora. O identificador fica de fora: o usuário escolhe carteiras pela lista. */
    public static String carteira(CarteiraDTO carteira) {
        return Mensagens.get("carteira.formato", carteira.getNomeTitular(), carteira.getCorretora());
    }

    /** Evita exibir "-0,00" para valores que arredondam para zero. */
    private static double zerarSeInsignificante(double valor) {
        return Math.abs(valor) < ARREDONDA_ZERO ? 0 : valor;
    }
}
