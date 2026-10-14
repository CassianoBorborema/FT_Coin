package config;

import exception.AppException;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Textos da aplicação no idioma escolhido, lidos de {@code i18n/mensagens_<idioma>.properties}.
 * O idioma vale para a aplicação inteira e começa em português.
 */
public final class Mensagens {

    /** Códigos dos idiomas disponíveis, na ordem em que são oferecidos ao usuário. */
    public static final List<String> IDIOMAS = List.of("pt", "en", "es");

    private static final String PACOTE = "i18n.mensagens";

    private static Locale locale;
    private static ResourceBundle textos;
    private static DateTimeFormatter formatoData;

    static {
        usar("pt");
    }

    private Mensagens() {
    }

    /**
     * @param idioma um dos {@link #IDIOMAS}, sem diferenciar maiúsculas
     * @throws AppException se o idioma não estiver disponível
     */
    public static void definirIdioma(String idioma) throws AppException {
        String codigo = idioma.trim().toLowerCase(Locale.ROOT);
        if (!IDIOMAS.contains(codigo)) {
            throw new AppException("erro.idioma", idioma, String.join(", ", IDIOMAS));
        }
        usar(codigo);
    }

    private static void usar(String codigo) {
        locale = switch (codigo) {
            case "pt" -> Locale.of("pt", "BR");
            case "en" -> Locale.US;
            default -> Locale.of("es", "ES");
        };
        textos = ResourceBundle.getBundle(PACOTE, Locale.of(codigo),
                ResourceBundle.Control.getNoFallbackControl(ResourceBundle.Control.FORMAT_PROPERTIES));
        // STRICT recusa datas como 31/02 em vez de ajustá-las; nele, "yyyy" exigiria a era, daí "uuuu".
        formatoData = DateTimeFormatter.ofPattern(padraoData().replace("yyyy", "uuuu"))
                .withResolverStyle(ResolverStyle.STRICT);
    }

    public static String idioma() {
        return locale.getLanguage();
    }

    public static Locale locale() {
        return locale;
    }

    /** Padrão de data do idioma, usado para exibir e para ler datas (ex.: {@code dd/MM/yyyy}). */
    public static String padraoData() {
        return textos.getString("formato.data");
    }

    public static String data(LocalDate data) {
        return data.format(formatoData);
    }

    public static LocalDate lerData(String texto) {
        return LocalDate.parse(texto, formatoData);
    }

    /**
     * Texto da chave no idioma atual, com os argumentos aplicados por {@link MessageFormat}. Antes
     * disso, datas são formatadas no padrão do idioma, inteiros viram texto (sem separador de
     * milhar) e exceções viram a própria mensagem, o que permite aninhar mensagens.
     *
     * @return o texto traduzido, ou a própria chave se ela não existir, para que um texto livre
     *         passado no lugar da chave seja exibido como está
     */
    public static String get(String chave, Object... argumentos) {
        if (chave == null || !textos.containsKey(chave)) {
            return chave;
        }
        Object[] convertidos = new Object[argumentos.length];
        for (int i = 0; i < argumentos.length; i++) {
            convertidos[i] = switch (argumentos[i]) {
                case LocalDate d -> data(d);
                case Integer n -> String.valueOf(n);
                case Long n -> String.valueOf(n);
                case Throwable t -> t.getMessage();
                case null, default -> argumentos[i];
            };
        }
        return new MessageFormat(textos.getString(chave), locale).format(convertidos);
    }
}
