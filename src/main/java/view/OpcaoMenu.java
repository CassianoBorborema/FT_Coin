package view;

import exception.AppException;

/**
 * Opção de um {@link Menu}.
 */
public record OpcaoMenu(String rotulo, Acao acao) {

    @FunctionalInterface
    public interface Acao {
        void executar() throws AppException;
    }
}
