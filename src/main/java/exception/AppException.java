package exception;

import config.Mensagens;

/**
 * Erro de negócio exibido ao usuário. Guarda a chave da mensagem e os argumentos; o texto só é
 * montado quando a mensagem é lida, no idioma escolhido naquele momento.
 */
public class AppException extends Exception {

    private static final long serialVersionUID = 1L;

    private final String chave;
    private final transient Object[] argumentos;

    /**
     * @param chave      chave em {@code i18n/mensagens}; um texto que não seja chave é exibido como está
     * @param argumentos valores da mensagem; outra exceção pode ser argumento e entra com a sua mensagem
     */
    public AppException(String chave, Object... argumentos) {
        super(chave);
        this.chave = chave;
        this.argumentos = argumentos;
    }

    public AppException(Throwable causa, String chave, Object... argumentos) {
        super(chave, causa);
        this.chave = chave;
        this.argumentos = argumentos;
    }

    public String getChave() {
        return chave;
    }

    @Override
    public String getMessage() {
        return Mensagens.get(chave, argumentos);
    }
}
