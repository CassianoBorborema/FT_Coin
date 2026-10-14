package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Comportamento que toda implementação de {@link FabricaDAO} deve ter. Cada persistência
 * estende esta classe e informa como criar uma fábrica com os dados vazios.
 */
public abstract class FabricaDAOContrato {

    private static final double TOLERANCIA = 1e-9;
    private static final LocalDate DIA_1 = LocalDate.of(2026, 1, 1);
    private static final LocalDate DIA_2 = DIA_1.plusDays(1);

    private CarteiraDAO carteiras;
    private MovimentacaoDAO movimentacoes;
    private OraculoDAO cotacoes;

    protected abstract FabricaDAO criarFabricaVazia() throws Exception;

    @BeforeEach
    void criarDAOs() throws Exception {
        FabricaDAO fabrica = criarFabricaVazia();
        carteiras = fabrica.carteiraDAO();
        movimentacoes = fabrica.movimentacaoDAO();
        cotacoes = fabrica.oraculoDAO();
    }

    @Test
    void incluirCarteiraGeraIdentificador() throws AppException {
        CarteiraDTO ana = new CarteiraDTO(0, "Ana", "XP");
        CarteiraDTO bruno = new CarteiraDTO(0, "Bruno", "Rico");

        carteiras.incluir(ana);
        carteiras.incluir(bruno);

        assertTrue(ana.getIdentificador() > 0);
        assertNotEquals(ana.getIdentificador(), bruno.getIdentificador());
        assertEquals("Ana", carteiras.consultarPorId(ana.getIdentificador()).getNomeTitular());
    }

    @Test
    void incluirCarteiraComIdentificadorRepetidoFalha() throws AppException {
        carteiras.incluir(new CarteiraDTO(7, "Ana", "XP"));

        assertThrows(AppException.class, () -> carteiras.incluir(new CarteiraDTO(7, "Bruno", "Rico")));
        assertEquals("XP", carteiras.consultarPorId(7).getCorretora());
    }

    @Test
    void consultarCarteiraInexistenteFalha() {
        assertThrows(AppException.class, () -> carteiras.consultarPorId(999));
    }

    @Test
    void listarTodasAsCarteiras() throws AppException {
        carteiras.incluir(new CarteiraDTO(0, "Ana", "XP"));
        carteiras.incluir(new CarteiraDTO(0, "Bruno", "Rico"));

        List<String> titulares = carteiras.listarTodas().stream()
                .map(CarteiraDTO::getNomeTitular).sorted().collect(Collectors.toList());

        assertEquals(List.of("Ana", "Bruno"), titulares);
    }

    @Test
    void atualizarCarteira() throws AppException {
        CarteiraDTO ana = new CarteiraDTO(0, "Ana", "XP");
        carteiras.incluir(ana);

        carteiras.atualizar(new CarteiraDTO(ana.getIdentificador(), "Ana Maria", "BTG"));

        CarteiraDTO atualizada = carteiras.consultarPorId(ana.getIdentificador());
        assertEquals("Ana Maria", atualizada.getNomeTitular());
        assertEquals("BTG", atualizada.getCorretora());
    }

    @Test
    void atualizarOuExcluirCarteiraInexistenteFalha() {
        assertThrows(AppException.class, () -> carteiras.atualizar(new CarteiraDTO(999, "Ana", "XP")));
        assertThrows(AppException.class, () -> carteiras.excluir(999));
    }

    @Test
    void excluirCarteira() throws AppException {
        CarteiraDTO ana = new CarteiraDTO(0, "Ana", "XP");
        carteiras.incluir(ana);

        carteiras.excluir(ana.getIdentificador());

        assertFalse(carteiras.existe(ana.getIdentificador()));
    }

    @Test
    void textoComAcentosEhPreservado() throws AppException {
        CarteiraDTO carteira = new CarteiraDTO(0, "João Ação", "Corretora Ômega");
        carteiras.incluir(carteira);

        CarteiraDTO lida = carteiras.consultarPorId(carteira.getIdentificador());
        assertEquals("João Ação", lida.getNomeTitular());
        assertEquals("Corretora Ômega", lida.getCorretora());
    }

    @Test
    void incluirEConsultarMovimentacao() throws AppException {
        int carteira = novaCarteira();
        MovimentacaoDTO compra = new MovimentacaoDTO(0, carteira, DIA_1, TipoMovimentacao.COMPRA, 12.5);

        movimentacoes.incluir(compra);

        MovimentacaoDTO lida = movimentacoes.consultarPorId(compra.getIdMovimento());
        assertEquals(carteira, lida.getIdCarteira());
        assertEquals(DIA_1, lida.getData());
        assertEquals(TipoMovimentacao.COMPRA, lida.getTipo());
        assertEquals(12.5, lida.getQuantidade(), TOLERANCIA);
    }

    @Test
    void quantidadeFracionariaEhPreservada() throws AppException {
        int carteira = novaCarteira();
        MovimentacaoDTO compra = new MovimentacaoDTO(0, carteira, DIA_1, TipoMovimentacao.COMPRA, 0.12345678);

        movimentacoes.incluir(compra);

        assertEquals(0.12345678, movimentacoes.consultarPorId(compra.getIdMovimento()).getQuantidade(), TOLERANCIA);
    }

    @Test
    void consultarMovimentacaoInexistenteFalha() {
        assertThrows(AppException.class, () -> movimentacoes.consultarPorId(999));
    }

    @Test
    void listarSoAsMovimentacoesDaCarteira() throws AppException {
        int ana = novaCarteira();
        int bruno = novaCarteira();
        movimentacoes.incluir(new MovimentacaoDTO(0, ana, DIA_1, TipoMovimentacao.COMPRA, 10));
        movimentacoes.incluir(new MovimentacaoDTO(0, bruno, DIA_1, TipoMovimentacao.COMPRA, 20));
        movimentacoes.incluir(new MovimentacaoDTO(0, ana, DIA_2, TipoMovimentacao.VENDA, 3));

        List<MovimentacaoDTO> daAna = movimentacoes.listarPorCarteira(ana);

        assertEquals(2, daAna.size());
        assertTrue(daAna.stream().allMatch(m -> m.getIdCarteira() == ana));
    }

    @Test
    void saldoSomaComprasESubtraiVendas() throws AppException {
        int carteira = novaCarteira();
        assertEquals(0, movimentacoes.calcularSaldo(carteira), TOLERANCIA);

        movimentacoes.incluir(new MovimentacaoDTO(0, carteira, DIA_1, TipoMovimentacao.COMPRA, 10));
        movimentacoes.incluir(new MovimentacaoDTO(0, carteira, DIA_2, TipoMovimentacao.VENDA, 3.5));

        assertEquals(6.5, movimentacoes.calcularSaldo(carteira), TOLERANCIA);
    }

    @Test
    void possuiMovimentacoes() throws AppException {
        int comMovimento = novaCarteira();
        int semMovimento = novaCarteira();
        movimentacoes.incluir(new MovimentacaoDTO(0, comMovimento, DIA_1, TipoMovimentacao.COMPRA, 1));

        assertTrue(movimentacoes.possuiMovimentacoes(comMovimento));
        assertFalse(movimentacoes.possuiMovimentacoes(semMovimento));
    }

    @Test
    void incluirEConsultarCotacao() throws AppException {
        cotacoes.incluir(new OraculoDTO(DIA_1, 12.28794));

        assertTrue(cotacoes.existe(DIA_1));
        assertFalse(cotacoes.existe(DIA_2));
        assertEquals(12.28794, cotacoes.consultarPorData(DIA_1).getCotacao(), TOLERANCIA);
    }

    @Test
    void cotacaoRepetidaOuInvalidaFalha() throws AppException {
        cotacoes.incluir(new OraculoDTO(DIA_1, 10));

        assertThrows(AppException.class, () -> cotacoes.incluir(new OraculoDTO(DIA_1, 11)));
        assertThrows(AppException.class, () -> cotacoes.incluir(new OraculoDTO(DIA_2, 0)));
        assertThrows(AppException.class, () -> cotacoes.consultarPorData(DIA_2));
    }

    @Test
    void listarCotacoesEmOrdemCronologica() throws AppException {
        cotacoes.incluir(new OraculoDTO(DIA_2, 11));
        cotacoes.incluir(new OraculoDTO(DIA_1, 10));

        assertEquals(List.of(DIA_1, DIA_2),
                cotacoes.listarTodas().stream().map(OraculoDTO::getData).collect(Collectors.toList()));
    }

    private int novaCarteira() throws AppException {
        CarteiraDTO carteira = new CarteiraDTO(0, "Titular", "Corretora");
        carteiras.incluir(carteira);
        return carteira.getIdentificador();
    }
}
