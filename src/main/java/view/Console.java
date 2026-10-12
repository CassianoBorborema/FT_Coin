package view;

import exception.AppException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Entrada e saída de texto da aplicação. Toda leitura é feita linha a linha,
 * o que evita sobras no buffer entre uma leitura numérica e a próxima.
 */
public class Console {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Valor devolvido por {@link #lerOpcao()} quando o texto digitado não é um número. */
    public static final int OPCAO_INVALIDA = -1;

    private final Scanner entrada;
    private final PrintStream saida;

    /**
     * @param entrada origem do texto digitado
     * @param saida   destino das mensagens
     */
    public Console(InputStream entrada, PrintStream saida) {
        this.entrada = new Scanner(entrada);
        this.saida = saida;
    }

    public void exibir(String mensagem) {
        saida.println(mensagem);
    }

    public void exibirFormatado(String formato, Object... argumentos) {
        saida.printf(formato, argumentos);
    }

    public void exibirErro(String mensagem) {
        saida.println("Erro: " + mensagem);
    }

    /**
     * Lê o número de uma opção de menu.
     *
     * @return o número digitado, ou {@link #OPCAO_INVALIDA} se o texto não for um número
     * @throws java.util.NoSuchElementException se a entrada tiver terminado
     */
    public int lerOpcao() {
        try {
            return Integer.parseInt(entrada.nextLine().trim());
        } catch (NumberFormatException e) {
            return OPCAO_INVALIDA;
        }
    }

    /**
     * @param mensagem texto exibido antes da leitura
     * @return a linha digitada, sem espaços nas pontas
     * @throws java.util.NoSuchElementException se a entrada tiver terminado
     */
    public String lerLinha(String mensagem) {
        saida.println(mensagem);
        return entrada.nextLine().trim();
    }

    public int lerInteiro(String mensagem) throws AppException {
        String texto = lerLinha(mensagem);
        try {
            return Integer.parseInt(texto);
        } catch (NumberFormatException e) {
            throw new AppException("Valor numérico inválido: " + texto + ".");
        }
    }

    /**
     * Lê um número decimal. Aceita vírgula ou ponto como separador decimal.
     *
     * @param mensagem texto exibido antes da leitura
     * @return o número digitado
     * @throws AppException se o texto não for um número
     */
    public double lerDecimal(String mensagem) throws AppException {
        String texto = lerLinha(mensagem);
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new AppException("Valor decimal inválido: " + texto + ".");
        }
    }

    public LocalDate lerData(String mensagem) throws AppException {
        String texto = lerLinha(mensagem + " (dd/MM/yyyy):");
        try {
            return LocalDate.parse(texto, FORMATO_DATA);
        } catch (DateTimeParseException e) {
            throw new AppException("Data inválida: " + texto + ". Use o formato dd/MM/yyyy.");
        }
    }

    /**
     * @param pergunta pergunta de sim ou não
     * @return {@code true} se o usuário responder S
     */
    public boolean confirmar(String pergunta) {
        return lerLinha(pergunta + " (S/N):").equalsIgnoreCase("S");
    }
}
