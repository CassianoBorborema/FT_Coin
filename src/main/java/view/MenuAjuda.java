package view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Guia de uso, fluxos de cada área e créditos.
 */
public class MenuAjuda extends Menu {


    public MenuAjuda(Console console) {
        super(console, "Ajuda");
        adicionarOpcao("Como funciona", () -> exibirTexto("ajuda"));
        adicionarOpcao("Fluxo de carteira", () -> exibirTexto("fluxo-carteira"));
        adicionarOpcao("Fluxo de movimentação", () -> exibirTexto("fluxo-movimentacao"));
        adicionarOpcao("Fluxo de relatórios", () -> exibirTexto("fluxo-relatorios"));
        adicionarOpcao("Créditos", () -> exibirTexto("creditos"));
    }

    private void exibirTexto(String nome) {
        console.exibir("");
        console.exibir(lerRecurso("/textos/" + nome + ".txt"));
    }

    private String lerRecurso(String caminho) {
        try (InputStream in = getClass().getResourceAsStream(caminho)) {
            if (in == null) {
                return "Texto indisponível (arquivo não encontrado: " + caminho + ").";
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException _) {
            return "Texto indisponível (erro ao ler: " + caminho + ").";
        }
    }
}
