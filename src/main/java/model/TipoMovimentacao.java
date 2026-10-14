package model;

import config.Mensagens;
import exception.AppException;
import java.time.LocalDate;

/**
 * Tipo de movimentação. Cada tipo sabe como afeta a {@link Posicao} da carteira. Em compras e
 * vendas a quantidade é de FTCoins; em créditos e saques, é o valor em reais.
 */
public enum TipoMovimentacao {

    COMPRA('C', 1) {
        @Override
        public double aplicar(Posicao posicao, LocalDate data, double quantidade, double cotacao)
                throws AppException {
            posicao.comprar(data, quantidade, cotacao);
            return 0;
        }
    },

    VENDA('V', -1) {
        @Override
        public double aplicar(Posicao posicao, LocalDate data, double quantidade, double cotacao)
                throws AppException {
            return posicao.vender(quantidade, cotacao);
        }
    },

    CREDITO('D', 0) {
        @Override
        public double aplicar(Posicao posicao, LocalDate data, double valor, double cotacao) {
            posicao.creditar(valor);
            return 0;
        }

        @Override
        public double valor(double valor, double cotacao) {
            return valor;
        }
    },

    SAQUE('S', 0) {
        @Override
        public double aplicar(Posicao posicao, LocalDate data, double valor, double cotacao)
                throws AppException {
            posicao.sacar(valor);
            return 0;
        }

        @Override
        public double valor(double valor, double cotacao) {
            return valor;
        }
    };

    private final char codigo;
    private final int sinal;

    TipoMovimentacao(char codigo, int sinal) {
        this.codigo = codigo;
        this.sinal = sinal;
    }

    /** Nome do tipo no idioma escolhido. */
    public String getDescricao() {
        return Mensagens.get("tipo." + name());
    }

    public char getCodigo() {
        return codigo;
    }

    /** +1 se aumenta o saldo de FTCoins, -1 se reduz, 0 se não movimenta FTCoins. */
    public int getSinal() {
        return sinal;
    }

    public boolean movimentaFTCoins() {
        return sinal != 0;
    }

    /**
     * @return o resultado da operação (só vendas têm resultado)
     * @throws AppException se a operação for inválida para a posição
     */
    public abstract double aplicar(Posicao posicao, LocalDate data, double quantidade, double cotacao)
            throws AppException;

    /** Valor financeiro da movimentação, em reais. */
    public double valor(double quantidade, double cotacao) {
        return quantidade * cotacao;
    }

    public static TipoMovimentacao fromChar(char codigo) throws AppException {
        for (TipoMovimentacao tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new AppException("erro.movimentacao.tipoInvalido", String.valueOf(codigo));
    }
}
