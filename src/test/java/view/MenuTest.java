package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import exception.AppException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class MenuTest {

    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();

    /** Menu de teste que registra as opções executadas. */
    private static class MenuExemplo extends Menu {

        private final List<String> executadas = new ArrayList<>();

        MenuExemplo(Console console) {
            super(console, "Exemplo");
            adicionarOpcao("Primeira", () -> executadas.add("primeira"));
            adicionarOpcao("Com erro", () -> {
                throw new AppException("falhou de propósito");
            });
        }
    }

    @Test
    void executaOpcaoEscolhidaAteVoltar() {
        MenuExemplo menu = new MenuExemplo(console("1\n1\n0\n"));

        menu.exibir();

        assertEquals(List.of("primeira", "primeira"), menu.executadas);
        assertTrue(texto().contains("1. Primeira"));
        assertTrue(texto().contains("0. Voltar"));
    }

    @Test
    void opcaoInexistenteOuNaoNumericaEhInvalida() {
        MenuExemplo menu = new MenuExemplo(console("9\nabc\n0\n"));

        menu.exibir();

        assertTrue(menu.executadas.isEmpty());
        assertEquals(2, texto().split("Opção inválida", -1).length - 1);
    }

    @Test
    void erroDeNegocioEhExibidoEMenuContinua() {
        MenuExemplo menu = new MenuExemplo(console("2\n1\n0\n"));

        menu.exibir();

        assertTrue(texto().contains("Erro: falhou de propósito"));
        assertEquals(List.of("primeira"), menu.executadas);
    }

    @Test
    void fimDaEntradaEncerraOMenu() {
        MenuExemplo menu = new MenuExemplo(console("1\n"));

        assertThrows(NoSuchElementException.class, menu::exibir);
    }

    @Test
    void consoleAceitaVirgulaComoSeparadorDecimal() throws AppException {
        assertEquals(10.5, console("10,5\n").lerDecimal("Quantidade:"));
        assertEquals(0.25, console("0.25\n").lerDecimal("Quantidade:"));
    }

    @Test
    void consoleRecusaDataForaDoFormato() throws AppException {
        assertEquals(LocalDate.of(2026, 1, 30), console("30/01/2026\n").lerData("Data"));
        assertThrows(AppException.class, () -> console("2026-01-30\n").lerData("Data"));
    }

    private Console console(String entrada) {
        return new Console(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(saida, true, StandardCharsets.UTF_8));
    }

    private String texto() {
        return saida.toString(StandardCharsets.UTF_8);
    }
}
