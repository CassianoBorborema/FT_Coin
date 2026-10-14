package view;

import config.Mensagens;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Guia de uso, fluxos de cada área e créditos, no idioma escolhido ({@code textos/<idioma>/}).
 */
public class MenuAjuda extends Menu {

    public MenuAjuda(Console console) {
        super(console, Mensagens.get("ajuda.titulo"));
        adicionarOpcao(Mensagens.get("ajuda.comoFunciona"), () -> exibirTexto("ajuda"));
        adicionarOpcao(Mensagens.get("ajuda.fluxoCarteira"), () -> exibirTexto("fluxo-carteira"));
        adicionarOpcao(Mensagens.get("ajuda.fluxoMovimentacao"), () -> exibirTexto("fluxo-movimentacao"));
        adicionarOpcao(Mensagens.get("ajuda.fluxoRelatorios"), () -> exibirTexto("fluxo-relatorios"));
        adicionarOpcao(Mensagens.get("ajuda.creditos"), () -> exibirTexto("creditos"));
    }

    private void exibirTexto(String nome) {
        console.exibir("");
        console.exibir(lerRecurso("/textos/" + Mensagens.idioma() + "/" + nome + ".txt"));
    }

    private String lerRecurso(String caminho) {
        try (InputStream in = getClass().getResourceAsStream(caminho)) {
            if (in == null) {
                return Mensagens.get("ajuda.textoNaoEncontrado", caminho);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException _) {
            return Mensagens.get("ajuda.textoComErro", caminho);
        }
    }
}
