package controller;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dto.ApuracaoDTO;
import dto.CarteiraDTO;
import exception.AppException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import model.ApuracaoCarteira;
import model.ProvedorCotacao;

public class RelatorioController {

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final ProvedorCotacao cotacoes;
    private final Clock relogio;

    public RelatorioController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO,
                               ProvedorCotacao cotacoes, Clock relogio) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
        this.cotacoes = cotacoes;
        this.relogio = relogio;
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
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(
                movimentacaoDAO.listarPorCarteira(idCarteira), cotacoes
        );
        return apuracao.toDTO(idCarteira, cotacoes.ultimaCotacaoAte(LocalDate.now(relogio)));
    }

    /** Uma linha por dia, da primeira movimentação até hoje, como na planilha de apuração. */
    public List<ApuracaoDTO.Dia> evolucaoDiaria(int idCarteira) throws AppException {
        carteiraDAO.validarExistente(idCarteira);
        return ApuracaoCarteira.evolucaoDiaria(
                movimentacaoDAO.listarPorCarteira(idCarteira), cotacoes, LocalDate.now(relogio));
    }
}
