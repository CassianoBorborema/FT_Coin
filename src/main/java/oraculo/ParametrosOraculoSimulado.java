package oraculo;

import config.Configuracao;
import exception.AppException;
import java.time.LocalDate;

/**
 * Parâmetros da fórmula de cotação do {@link OraculoSimulado}.
 *
 * @param dataInicial        primeiro dia com cotação (dia 1 da valorização)
 * @param base               valor base da cotação
 * @param amplitude          limite da flutuação diária, para mais e para menos
 * @param valorizacaoInicial valorização no dia 1
 * @param fatorValorizacao   fator aplicado a {@code ln(n)} na valorização
 * @param semente            semente do sorteio da flutuação
 */
public record ParametrosOraculoSimulado(LocalDate dataInicial, double base, double amplitude,
                                        double valorizacaoInicial, double fatorValorizacao, long semente) {

    /**
     * Valida os parâmetros. A cotação mínima possível (base + valorização inicial - amplitude)
     * precisa ser positiva, já que a valorização nunca diminui.
     *
     * @throws IllegalArgumentException se algum parâmetro for inválido
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

    /**
     * Lê os parâmetros das chaves {@code oraculo.*} da configuração.
     *
     * @param configuracao configuração da aplicação
     * @return os parâmetros do oráculo simulado
     * @throws AppException se alguma chave faltar ou tiver valor inválido
     */
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
