package view;

import exception.AppException;

/**
 * Opção de um {@link Menu}: o texto exibido e a ação executada quando ela é escolhida.
 *
 * @param rotulo texto da opção
 * @param acao   ação executada ao escolher a opção
 */
public record OpcaoMenu(String rotulo, Acao acao) {

    /** Ação de uma opção de menu. Erros de negócio são exibidos pelo próprio menu. */
    @FunctionalInterface
    public interface Acao {
        void executar() throws AppException;
    }
}
