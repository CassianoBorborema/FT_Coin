package view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import controller.RelatorioController;
import dao.Persistencia;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import testutil.CotacoesFixas;

class MenuRelatoriosTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);

    @TempDir
    Path diretorio;

    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();
    private RelatorioController controller;

    @BeforeEach
    void setUp() throws AppException {
        Persistencia persistencia = Persistencia.emMemoria();
        persistencia.carteiras().incluir(new CarteiraDTO(0, "Ana", "XP"));
        persistencia.movimentacoes().incluir(new MovimentacaoDTO(0, 1, HOJE.minusDays(1), TipoMovimentacao.COMPRA, 10));
        controller = new RelatorioController(persistencia.carteiras(), persistencia.movimentacoes(),
                new CotacoesFixas().com(HOJE.minusDays(1), 10.0).com(HOJE, 12.0), CotacoesFixas.relogioEm(HOJE));
    }

    @Test
    void saldoImpressoVaiParaArquivo() throws IOException {
        executar("3\n1\nS\n0\n");

        Path arquivo = diretorio.resolve("saldo-carteira-1-2026-01-30.txt");
        assertTrue(Files.exists(arquivo));
        String conteudo = Files.readString(arquivo, StandardCharsets.UTF_8);
        assertTrue(conteudo.contains("Saldo atual da carteira 1 em 30/01/2026"));
        assertTrue(conteudo.contains("R$ 120,00"));
        assertTrue(texto().contains("Saldo impresso em " + arquivo));
    }

    @Test
    void saldoSemImpressaoNaoCriaArquivo() throws IOException {
        executar("3\n1\nN\n0\n");

        try (var arquivos = Files.list(diretorio)) {
            assertFalse(arquivos.findAny().isPresent());
        }
    }

    @Test
    void ganhosEPerdasNoFormatoDaPlanilha() {
        executar("6\n0\n");

        assertTrue(texto().contains("Você vendeu suas moedas por R$ 0,00 e as havia comprado por R$ 100,00."));
        assertTrue(texto().contains("Teve prejuízo operacional de R$ -100,00."));
        assertTrue(texto().contains("seu resultado passaria a ser R$ 20,00 (20,00%)."));
    }

    @Test
    void evolucaoDiariaMostraUmaLinhaPorDia() {
        executar("5\n1\n0\n");

        assertTrue(texto().contains("29/01/2026"));
        assertTrue(texto().contains("30/01/2026"));
        assertTrue(texto().contains("Totais: compras de 10 FTCoins (R$ 100,00)"));
    }

    @Test
    void textosDeAjudaExistem() {
        new MenuAjuda(console("1\n2\n3\n4\n5\n0\n")).exibir();

        assertFalse(texto().contains("Texto indisponível"));
        assertTrue(texto().contains("=== FLUXO DE CARTEIRA ==="));
        assertTrue(texto().contains("=== FLUXO DE MOVIMENTAÇÃO ==="));
        assertTrue(texto().contains("=== FLUXO DE RELATÓRIOS ==="));
    }

    private void executar(String entrada) {
        new MenuRelatorios(console(entrada), controller).exibir();
    }

    private Console console(String entrada) {
        return new Console(new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(saida, true, StandardCharsets.UTF_8), diretorio);
    }

    private String texto() {
        return saida.toString(StandardCharsets.UTF_8);
    }
}
