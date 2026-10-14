package testutil;

import exception.AppException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import model.ProvedorCotacao;

/**
 * Cotações definidas pelo próprio teste.
 */
public class CotacoesFixas implements ProvedorCotacao {

    private final Map<LocalDate, Double> cotacoes = new HashMap<>();
    private int consultas;

    public CotacoesFixas com(LocalDate data, double cotacao) {
        cotacoes.put(data, cotacao);
        return this;
    }

    @Override
    public double cotacaoEm(LocalDate data) throws AppException {
        consultas++;
        Double cotacao = cotacoes.get(data);
        if (cotacao == null) {
            throw new AppException("Sem cotação para " + data + ".");
        }
        return cotacao;
    }

    public int getConsultas() {
        return consultas;
    }

    public static Clock relogioEm(LocalDate hoje) {
        return Clock.fixed(hoje.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }
}
