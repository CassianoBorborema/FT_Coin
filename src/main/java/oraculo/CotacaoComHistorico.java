package oraculo;

import dao.OraculoDAO;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import model.ProvedorCotacao;

/**
 * Registra no {@link OraculoDAO} cada cotação obtida da fonte e responde pelo registro nas
 * consultas seguintes (Decorator). Assim a cotação de uma data não muda depois de usada, mesmo
 * que a fonte seja trocada pela API do Oráculo.
 */
public class CotacaoComHistorico implements ProvedorCotacao {

    private final ProvedorCotacao fonte;
    private final OraculoDAO historico;

    public CotacaoComHistorico(ProvedorCotacao fonte, OraculoDAO historico) {
        this.fonte = fonte;
        this.historico = historico;
    }

    @Override
    public double cotacaoEm(LocalDate data) throws AppException {
        if (data != null && historico.existe(data)) {
            return historico.consultarPorData(data).getCotacao();
        }
        double cotacao = fonte.cotacaoEm(data);
        historico.incluir(new OraculoDTO(data, cotacao));
        return cotacao;
    }
}
