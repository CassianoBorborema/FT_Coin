package exception;

/**
 * Falha de acesso ao banco de dados. A {@code SQLException} original fica em {@link #getCause()}.
 */
public class PersistenciaException extends AppException {

    private static final long serialVersionUID = 1L;

    public PersistenciaException(Throwable causa, String chave, Object... argumentos) {
        super(causa, chave, argumentos);
    }
}
