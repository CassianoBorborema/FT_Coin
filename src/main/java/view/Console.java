package view;

import exception.AppException;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

/**
 * Entrada e saída de texto. Toda leitura é por linha, para não sobrar texto no buffer entre uma
 * leitura numérica e a próxima.
 */
public class Console {

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static final int OPCAO_INVALIDA = -1;

    private final Scanner entrada;
    private final PrintStream saida;

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
            throw new AppException("Valor numérico inválido: " + texto + ".");
        }
    }

    /** Aceita vírgula ou ponto como separador decimal. */
    public double lerDecimal(String mensagem) throws AppException {
        String texto = lerLinha(mensagem);
        try {
            return Double.parseDouble(texto.replace(',', '.'));
        } catch (NumberFormatException _) {
            throw new AppException("Valor decimal inválido: " + texto + ".");
        }
    }

    public LocalDate lerData(String mensagem) throws AppException {
        String texto = lerLinha(mensagem + " (dd/MM/yyyy):");
        try {
            return LocalDate.parse(texto, FORMATO_DATA);
        } catch (DateTimeParseException _) {
            throw new AppException("Data inválida: " + texto + ". Use o formato dd/MM/yyyy.");
        }
    }

    public boolean confirmar(String pergunta) {
        return lerLinha(pergunta + " (S/N):").equalsIgnoreCase("S");
    }
}
