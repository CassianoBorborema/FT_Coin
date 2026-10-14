package oraculo;

import config.Configuracao;
import exception.AppException;
import java.time.LocalDate;

/**
 * Parâmetros da fórmula do {@link OraculoSimulado}, lidos das chaves {@code oraculo.*}.
 */
public record ParametrosOraculoSimulado(LocalDate dataInicial, double base, double amplitude,
                                        double valorizacaoInicial, double fatorValorizacao, long semente) {

    /**
     * Como a valorização nunca diminui, a menor cotação possível é
     * {@code base + valorizacaoInicial - amplitude}, que precisa ser positiva.
     */
    public ParametrosOraculoSimulado {
        if (dataInicial == null) {
            throw new IllegalArgumentException("A data inicial do oráculo é obrigatória.");
        }
        if (amplitude < 0) {
            throw new IllegalArgumentException("A amplitude da flutuação não pode ser negativa.");
        }
        if (fatorValorizacao < 0) {
            throw new IllegalArgumentException("O fator de valorização não pode ser negativo.");
        }
        if (base + valorizacaoInicial - amplitude <= 0) {
            throw new IllegalArgumentException(
                    "Os parâmetros do oráculo permitem cotação menor ou igual a zero (base + valorização inicial - amplitude).");
        }
    }

    public static ParametrosOraculoSimulado carregar(Configuracao configuracao) throws AppException {
        try {
            return new ParametrosOraculoSimulado(
                    configuracao.getData("oraculo.dataInicial"),
                    configuracao.getDecimal("oraculo.base"),
                    configuracao.getDecimal("oraculo.flutuacao.amplitude"),
                    configuracao.getDecimal("oraculo.valorizacao.inicial"),
                    configuracao.getDecimal("oraculo.valorizacao.fator"),
                    configuracao.getInteiro("oraculo.semente"));
        } catch (IllegalArgumentException e) {
            throw new AppException(e.getMessage());
        }
    }
}
