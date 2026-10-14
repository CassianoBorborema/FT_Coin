package view;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Guia de uso e créditos da aplicação.
 */
public class MenuAjuda extends Menu {

    private static final String CAMINHO_TEXTO_AJUDA = "/textos/ajuda.txt";
    private static final String CAMINHO_CREDITOS = "/textos/creditos.txt";

    public MenuAjuda(Console console) {
        super(console, "Ajuda");
        adicionarOpcao("Como funciona", () -> exibirTexto(CAMINHO_TEXTO_AJUDA));
        adicionarOpcao("Créditos", () -> exibirTexto(CAMINHO_CREDITOS));
    }

    private void exibirTexto(String caminho) {
        console.exibir("");
        console.exibir(lerRecurso(caminho));
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
