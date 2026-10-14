package view;

import config.Mensagens;
import exception.AppException;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Entrada e saída de texto. Toda leitura é por linha, para não sobrar texto no buffer entre uma
 * leitura numérica e a próxima.
 */
public class Console {

    public static final int OPCAO_INVALIDA = -1;

    private final Scanner entrada;
    private final PrintStream saida;
    private final Path diretorioArquivos;

    /** Lê a entrada como UTF-8. */
    public Console(InputStream entrada, PrintStream saida) {
        this(entrada, saida, Path.of(""));
    }

    /**
     * @param diretorioArquivos onde {@link #salvarArquivo} grava os arquivos
     */
    public Console(InputStream entrada, PrintStream saida, Path diretorioArquivos) {
        this(new Scanner(entrada, StandardCharsets.UTF_8), saida, diretorioArquivos);
    }

    private Console(Scanner entrada, PrintStream saida, Path diretorioArquivos) {
        this.entrada = entrada;
        this.saida = saida;
        this.diretorioArquivos = diretorioArquivos;
    }

    /**
     * Console da entrada e saída padrão. Num terminal de verdade usa a codificação dele (no Windows,
     * a página de código do console); redirecionado ou no Git Bash, que esperam UTF-8, usa UTF-8.
     * Sem isso, os acentos saem trocados em um dos dois casos.
     */
    public static Console doTerminal() {
        java.io.Console terminal = System.console();
        boolean ehTerminal = terminal != null && terminal.isTerminal();
        Charset entrada = ehTerminal ? codificacao("stdin.encoding") : StandardCharsets.UTF_8;
        Charset saida = ehTerminal ? codificacao("stdout.encoding") : StandardCharsets.UTF_8;
        return new Console(new Scanner(System.in, entrada),
                new PrintStream(new FileOutputStream(FileDescriptor.out), true, saida), Path.of(""));
    }

    private static Charset codificacao(String propriedade) {
        Charset padrao = Charset.defaultCharset();
        return Charset.forName(System.getProperty(propriedade, padrao.name()), padrao);
    }

    public void exibir(String mensagem) {
        saida.println(mensagem);
    }

    public void exibirFormatado(String formato, Object... argumentos) {
        saida.printf(Mensagens.locale(), formato, argumentos);
    }

    public void exibirErro(String mensagem) {
        saida.println(Mensagens.get("console.erro", mensagem));
    }

    /**
     * @return o número digitado, ou {@link #OPCAO_INVALIDA} se não for um número
     * @throws java.util.NoSuchElementException se a entrada tiver terminado
     */
    public int lerOpcao() {
        try {
            return Integer.parseInt(entrada.nextLine().trim());
        } catch (NumberFormatException _) {
            return OPCAO_INVALIDA;
        }
    }

    /** @throws java.util.NoSuchElementException se a entrada tiver terminado */
    public String lerLinha(String mensagem) {
        saida.println(mensagem);
        return entrada.nextLine().trim();
    }

    public int lerInteiro(String mensagem) throws AppException {
        String texto = lerLinha(mensagem);
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException _) {
            throw new AppException("erro.numeroInvalido", texto);
        }
    }

    /** Aceita vírgula ou ponto como separador decimal. */
    public double lerDecimal(String mensagem) throws AppException {
        String texto = lerLinha(mensagem);
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException _) {
            throw new AppException("erro.decimalInvalido", texto);
        }
    }

    /** Lê uma data no padrão do idioma ({@link Mensagens#padraoData()}). */
    public LocalDate lerData(String mensagem) throws AppException {
        String texto = lerLinha(mensagem + " (" + Mensagens.padraoData() + "):");
        try {
            return Mensagens.lerData(texto);
        } catch (DateTimeParseException _) {
            throw new AppException("erro.dataInvalida", texto, Mensagens.padraoData());
        }
    }

    /**
     * @return o caminho completo do arquivo gravado
     * @throws AppException se não for possível gravar
     */
    public Path salvarArquivo(String nome, List<String> linhas) throws AppException {
        Path arquivo = diretorioArquivos.resolve(nome).toAbsolutePath();
        try {
            Files.write(arquivo, linhas, StandardCharsets.UTF_8);
            return arquivo;
        } catch (IOException e) {
            throw new AppException(e, "erro.salvarArquivo", arquivo.toString(), e.getMessage());
        }
    }

    /** Pergunta de sim ou não, com as letras do idioma (ex.: S/N). */
    public boolean confirmar(String pergunta) {
        String sim = Mensagens.get("console.sim");
        String resposta = lerLinha(pergunta + " (" + sim + "/" + Mensagens.get("console.nao") + "):");
        return resposta.equalsIgnoreCase(sim);
    }
}
