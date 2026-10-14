package controller;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import exception.AppException;
import java.time.Clock;
import java.time.LocalDate;
import dto.MovimentacaoDTO;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.ApuracaoCarteira;
import model.ProvedorCotacao;

/**
 * Relatórios das carteiras. A apuração de cada carteira fica em cache até entrar uma movimentação
 * nova; a valoração a mercado usa sempre a última cotação, consultada a cada relatório.
 */
public class RelatorioController {

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final ProvedorCotacao cotacoes;
    private final Clock relogio;
    private final LocalDate vigenciaCredito;
    private final Map<Integer, ApuracaoCarteira> apuracoes = new HashMap<>();

    /**
     * @param vigenciaCredito a partir desta data as compras debitam o saldo em reais da carteira
     */
    public RelatorioController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO,
                               ProvedorCotacao cotacoes, Clock relogio, LocalDate vigenciaCredito) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
        this.cotacoes = cotacoes;
        this.relogio = relogio;
        this.vigenciaCredito = vigenciaCredito;
    }

    public List<CarteiraDTO> listarPorIdentificador() throws AppException {
        List<CarteiraDTO> carteiras = carteiraDAO.listarTodas();
        carteiras.sort(Comparator.comparingInt(CarteiraDTO::getIdentificador));
        return carteiras;
    }

    public List<CarteiraDTO> listarPorTitular() throws AppException {
        List<CarteiraDTO> carteiras = carteiraDAO.listarTodas();
        carteiras.sort(Comparator.comparing(
                CarteiraDTO::getNomeTitular, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
        ));
        return carteiras;
    }

    /**
     * Apura a carteira: saldo em FTCoins, custo médio, valoração a custo médio e a mercado
     * (pela última cotação disponível) e o histórico com a posição após cada operação.
     * Alimenta os relatórios de saldo atual, histórico e ganho/perda.
     *
     * @param idCarteira identificador da carteira
     * @return a apuração completa da carteira
     * @throws AppException se a carteira não existir, se faltar cotação no Oráculo
     *                      ou se o histórico da carteira for inconsistente
     */
    public ApuracaoDTO apurar(int idCarteira) throws AppException {
        carteiraDAO.validarExistente(idCarteira);
        return apuracaoEmCache(idCarteira).toDTO(idCarteira, cotacoes.ultimaCotacaoAte(LocalDate.now(relogio)));
    }

    private ApuracaoCarteira apuracaoEmCache(int idCarteira) throws AppException {
        List<MovimentacaoDTO> movimentacoes = movimentacaoDAO.listarPorCarteira(idCarteira);
        ApuracaoCarteira emCache = apuracoes.get(idCarteira);
        if (emCache != null && emCache.correspondeA(movimentacoes)) {
            return emCache;
        }
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(movimentacoes, cotacoes, vigenciaCredito);
        apuracoes.put(idCarteira, apuracao);
        return apuracao;
    }

    /** Uma linha por dia, da primeira movimentação até hoje, como na planilha de apuração. */
    public List<ApuracaoDTO.Dia> evolucaoDiaria(int idCarteira) throws AppException {
        carteiraDAO.validarExistente(idCarteira);
        return ApuracaoCarteira.evolucaoDiaria(
                movimentacaoDAO.listarPorCarteira(idCarteira), cotacoes, LocalDate.now(relogio), vigenciaCredito);
    }
}
