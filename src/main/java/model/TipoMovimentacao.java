package model;

import exception.AppException;

/**
 * Tipo de movimentação. Cada tipo sabe como afeta o saldo e a {@link Posicao} da carteira.
 */
public enum TipoMovimentacao {

    COMPRA('C', 1, "Compra") {
        @Override
        public double aplicar(Posicao posicao, double quantidade, double cotacao) {
            posicao.comprar(quantidade, cotacao);
            return 0;
        }
    },

    VENDA('V', -1, "Venda") {
        @Override
        public double aplicar(Posicao posicao, double quantidade, double cotacao) throws AppException {
            return posicao.vender(quantidade, cotacao);
        }
    };

    private final char codigo;
    private final int sinal;
    private final String descricao;

    TipoMovimentacao(char codigo, int sinal, String descricao) {
        this.codigo = codigo;
        this.sinal = sinal;
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    public char getCodigo() {
        return codigo;
    }

    /** +1 se aumenta o saldo de FTCoins, -1 se reduz. */
    public int getSinal() {
        return sinal;
    }

    /**
     * @return o resultado da operação (zero para compras)
     * @throws AppException se a operação for inválida para a posição
     */
    public abstract double aplicar(Posicao posicao, double quantidade, double cotacao) throws AppException;

    public static TipoMovimentacao fromChar(char codigo) throws AppException {
        for (TipoMovimentacao tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new AppException("Tipo de movimentação inválido: " + codigo + ". Use C (compra) ou V (venda).");
    }
}
