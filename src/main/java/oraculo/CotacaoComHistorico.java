package oraculo;

import dao.OraculoDAO;
import dto.OraculoDTO;
import exception.AppException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import model.ProvedorCotacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registra no {@link OraculoDAO} cada cotação obtida da fonte e responde pelo registro nas
 * consultas seguintes (Decorator). Assim a cotação de uma data não muda depois de usada, mesmo
 * que a fonte seja trocada pela API do Oráculo. Como uma cotação registrada nunca muda, ela também
 * fica em memória e o banco é consultado só uma vez por data.
 */
public class CotacaoComHistorico implements ProvedorCotacao {

    private static final Logger log = LoggerFactory.getLogger(CotacaoComHistorico.class);

    private final ProvedorCotacao fonte;
    private final OraculoDAO historico;
    private final Map<LocalDate, Double> emMemoria = new HashMap<>();

    public CotacaoComHistorico(ProvedorCotacao fonte, OraculoDAO historico) {
        this.fonte = fonte;
        this.historico = historico;
    }

    @Override
    public double cotacaoEm(LocalDate data) throws AppException {
        Double conhecida = emMemoria.get(data);
        if (conhecida != null) {
            return conhecida;
        }
        double cotacao;
        if (data != null && historico.existe(data)) {
            cotacao = historico.consultarPorData(data).getCotacao();
            log.debug("Cotação de {} lida do histórico: {}", data, cotacao);
        } else {
            cotacao = fonte.cotacaoEm(data);
            historico.incluir(new OraculoDTO(data, cotacao));
            log.info("Cotação de {} obtida da fonte e gravada no histórico: {}", data, cotacao);
        }
        emMemoria.put(data, cotacao);
        return cotacao;
    }
}
