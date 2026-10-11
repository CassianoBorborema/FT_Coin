package controller;

import DAO.CarteiraDAO;
import DAO.MovimentacaoDAO;
import DAO.OraculoDAO;
import DTO.ApuracaoDTO;
import DTO.CarteiraDTO;
import exception.AppException;
import model.ApuracaoCarteira;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class RelatorioController {

    private final CarteiraDAO carteiraDAO;
    private final MovimentacaoDAO movimentacaoDAO;
    private final OraculoDAO oraculoDAO;

    public RelatorioController(CarteiraDAO carteiraDAO, MovimentacaoDAO movimentacaoDAO, OraculoDAO oraculoDAO) {
        this.carteiraDAO = carteiraDAO;
        this.movimentacaoDAO = movimentacaoDAO;
        this.oraculoDAO = oraculoDAO;
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
     * (pela cotação de hoje) e o histórico de movimentações com a posição após cada operação.
     * Alimenta os relatórios de saldo atual, histórico e ganho/perda.
     *
     * @param idCarteira identificador da carteira
     * @return a apuração completa da carteira
     * @throws AppException se a carteira não existir, se faltar cotação no Oráculo
     *                      ou se o histórico da carteira for inconsistente
     */
    public ApuracaoDTO apurar(int idCarteira) throws AppException {
        validarCarteiraExiste(idCarteira);
        ApuracaoCarteira apuracao = ApuracaoCarteira.calcular(
                movimentacaoDAO.listarPorCarteira(idCarteira), this::cotacaoNaData
        );
        return apuracao.toDTO(idCarteira, cotacaoNaData(LocalDate.now()));
    }

    private double cotacaoNaData(LocalDate data) throws AppException {
        if (!oraculoDAO.existe(data)) {
            throw new AppException(
                    "Não há cotação cadastrada para a data " + data + ". Cadastre-a no menu Oráculo."
            );
        }
        return oraculoDAO.consultarPorData(data).getCotacao();
    }

    private void validarCarteiraExiste(int idCarteira) throws AppException {
        if (idCarteira <= 0) {
            throw new AppException("Identificador da carteira deve ser maior que zero.");
        }
        if (!carteiraDAO.existe(idCarteira)) {
            throw new AppException("Carteira com identificador " + idCarteira + " não encontrada.");
        }
    }
}
