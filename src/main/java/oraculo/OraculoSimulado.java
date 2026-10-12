package oraculo;

import exception.AppException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.SplittableRandom;
import model.ProvedorCotacao;

/**
 * Oráculo provisório, usado até a API de cotações existir. Gera a cotação de cada dia com a
 * fórmula da planilha de apuração de referência:
 * <pre>
 *   cotação     = base + flutuação + valorização
 *   valorização = valorizacaoInicial + fatorValorizacao * ln(n)   (n = 1 na data inicial)
 *   flutuação   = sorteio uniforme em [-amplitude, +amplitude)
 * </pre>
 * <p>
 * A flutuação usa uma semente derivada da data, então a cotação de um mesmo dia é sempre a mesma.
 * Não há cotação antes da data inicial nem depois de hoje.
 * </p>
 */
public class OraculoSimulado implements ProvedorCotacao {

    private static final int CASAS_DECIMAIS = 2;

    private final ParametrosOraculoSimulado parametros;
    private final Clock relogio;

    /**
     * @param parametros parâmetros da fórmula
     * @param relogio    relógio que define o dia de hoje
     */
    public OraculoSimulado(ParametrosOraculoSimulado parametros, Clock relogio) {
        this.parametros = parametros;
        this.relogio = relogio;
    }

    @Override
    public double cotacaoEm(LocalDate data) throws AppException {
        if (data == null) {
            throw new AppException("Data da cotação é obrigatória.");
        }
        if (data.isBefore(parametros.dataInicial())) {
            throw new AppException("O Oráculo só tem cotações a partir de " + parametros.dataInicial() + ".");
        }
        if (data.isAfter(LocalDate.now(relogio))) {
            throw new AppException("Não há cotação para a data futura " + data + ".");
        }
        double cotacao = parametros.base() + flutuacao(data) + valorizacao(data);
        return BigDecimal.valueOf(cotacao).setScale(CASAS_DECIMAIS, RoundingMode.HALF_UP).doubleValue();
    }

    /**
     * @param data data a partir da data inicial
     * @return a parcela de valorização da cotação na data
     */
    double valorizacao(LocalDate data) {
        long dia = ChronoUnit.DAYS.between(parametros.dataInicial(), data) + 1;
        return parametros.valorizacaoInicial() + parametros.fatorValorizacao() * Math.log(dia);
    }

    /**
     * @param data data da cotação
     * @return a parcela de flutuação da cotação na data, sempre a mesma para a mesma data
     */
    double flutuacao(LocalDate data) {
        double amplitude = parametros.amplitude();
        if (amplitude == 0) {
            return 0;
        }
        return new SplittableRandom(parametros.semente() + data.toEpochDay()).nextDouble(-amplitude, amplitude);
    }
}
