package model;

import exception.AppException;

public enum TipoMovimentacao {
    COMPRA('C'),
    VENDA('V');

    private final char codigo;

    TipoMovimentacao(char codigo) {
        this.codigo = codigo;
    }

    public char getCodigo() {
        return codigo;
    }

    public static TipoMovimentacao fromChar(char codigo) throws AppException {
        for (TipoMovimentacao tipo : values()) {
            if (tipo.codigo == codigo) {
                return tipo;
            }
        }
        throw new AppException("Tipo de movimentação inválido: " + codigo + ". Use C (compra) ou V (venda).");
    }
}
