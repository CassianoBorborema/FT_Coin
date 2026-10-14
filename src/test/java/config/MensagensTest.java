package config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.AppException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import model.TipoMovimentacao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import view.Formatador;

class MensagensTest {

    private static final Pattern LITERAL_DE_CHAVE = Pattern.compile("\"([a-z]+\\.[a-zA-Z.]+)\"");
    private static final Set<String> LITERAIS_QUE_NAO_SAO_MENSAGENS =
            Set.of("i18n.mensagens", "ftcoin.local.properties", "mariadb.logging.disable", "stdin.encoding", "stdout.encoding");

    @AfterEach
    void voltarAoPortugues() throws AppException {
        Mensagens.definirIdioma("pt");
    }

    @Test
    void osTresIdiomasTemAsMesmasChaves() throws IOException {
        Set<String> portugues = carregar("pt").stringPropertyNames();
        for (String idioma : Mensagens.IDIOMAS) {
            assertEquals(new TreeSet<>(portugues), new TreeSet<>(carregar(idioma).stringPropertyNames()), idioma);
        }
    }

    @Test
    void todasAsMensagensSaoPadroesValidos() throws IOException {
        for (String idioma : Mensagens.IDIOMAS) {
            Properties textos = carregar(idioma);
            for (String chave : textos.stringPropertyNames()) {
                assertDoesNotThrow(() -> new MessageFormat(textos.getProperty(chave)), idioma + ": " + chave);
            }
        }
    }

    /** Pega chave digitada errada no código, que apareceria crua para o usuário. */
    @Test
    void chavesUsadasNoCodigoExistem() throws IOException {
        Set<String> chaves = carregar("pt").stringPropertyNames();
        Properties configuracao = new Properties();
        try (InputStream in = getClass().getResourceAsStream("/ftcoin.properties")) {
            configuracao.load(in);
        }
        Set<String> ausentes = new TreeSet<>();
        try (Stream<Path> fontes = Files.walk(Path.of("src/main/java"))) {
            for (Path fonte : fontes.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher literal = LITERAL_DE_CHAVE.matcher(Files.readString(fonte));
                while (literal.find()) {
                    String chave = literal.group(1);
                    if (!chaves.contains(chave) && !configuracao.containsKey(chave)
                            && !chave.equals("db.senha") && !LITERAIS_QUE_NAO_SAO_MENSAGENS.contains(chave)) {
                        ausentes.add(fonte.getFileName() + ": " + chave);
                    }
                }
            }
        }
        assertTrue(ausentes.isEmpty(), "Chaves sem tradução: " + ausentes);
        for (TipoMovimentacao tipo : TipoMovimentacao.values()) {
            assertTrue(chaves.contains("tipo." + tipo.name()), tipo.name());
        }
    }

    @Test
    void mensagemDaExcecaoSaiNoIdiomaDoMomentoDaLeitura() throws AppException {
        AppException erro = new AppException("erro.carteira.naoEncontrada", 1234);
        assertEquals("Carteira com identificador 1234 não encontrada.", erro.getMessage());

        Mensagens.definirIdioma("en");
        assertEquals("Wallet with ID 1234 not found.", erro.getMessage());

        Mensagens.definirIdioma("es");
        assertEquals("Cartera con identificador 1234 no encontrada.", erro.getMessage());
    }

    @Test
    void excecaoComoArgumentoEntraTraduzida() throws AppException {
        AppException causa = new AppException("erro.cotacao.futura", LocalDate.of(2026, 1, 30));
        AppException erro = new AppException("erro.historico.inconsistente", LocalDate.of(2026, 1, 29), causa);

        Mensagens.definirIdioma("en");

        assertEquals("Inconsistent history on 01/29/2026: There is no price for the future date 01/30/2026.",
                erro.getMessage());
    }

    @Test
    void datasENumerosSeguemOIdioma() throws AppException {
        LocalDate data = LocalDate.of(2026, 1, 30);
        assertAll(
                () -> assertEquals("30/01/2026", Formatador.data(data)),
                () -> assertEquals("R$ 1.234,50", Formatador.moeda(1234.5)),
                () -> assertEquals("1.234,5", Formatador.quantidade(1234.5)));

        Mensagens.definirIdioma("en");
        assertAll(
                () -> assertEquals("01/30/2026", Formatador.data(data)),
                () -> assertEquals("R$ 1,234.50", Formatador.moeda(1234.5)),
                () -> assertEquals(data, Mensagens.lerData("01/30/2026")),
                () -> assertEquals("Withdrawal", TipoMovimentacao.SAQUE.getDescricao()));
    }

    @Test
    void dataInexistenteEhRecusada() {
        assertThrows(java.time.format.DateTimeParseException.class, () -> Mensagens.lerData("31/02/2026"));
    }

    @Test
    void textoQueNaoEChaveSaiComoEsta() {
        assertEquals("falhou de propósito", new AppException("falhou de propósito").getMessage());
    }

    @Test
    void idiomaIndisponivelEhRecusado() {
        AppException erro = assertThrows(AppException.class, () -> Mensagens.definirIdioma("fr"));
        assertTrue(erro.getMessage().contains("pt, en, es"));
        assertNotEquals("fr", Mensagens.idioma());
    }

    @Test
    void codigoDoIdiomaIgnoraMaiusculasEEspacos() throws AppException {
        Mensagens.definirIdioma(" EN ");
        assertEquals("en", Mensagens.idioma());
    }

    private static Properties carregar(String idioma) throws IOException {
        Properties textos = new Properties();
        try (InputStream in = MensagensTest.class.getResourceAsStream("/i18n/mensagens_" + idioma + ".properties")) {
            textos.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        }
        return textos;
    }
}
