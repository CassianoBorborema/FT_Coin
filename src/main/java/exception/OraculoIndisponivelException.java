package exception;

/**
 * O servidor do Oráculo não respondeu ou devolveu erro. Diferente de "não há cotação nesse dia",
 * a falha não deve ser tratada como ausência de cotação.
 */
public class OraculoIndisponivelException extends AppException {

    private static final long serialVersionUID = 1L;

    public OraculoIndisponivelException(Throwable causa, String detalhe) {
        super(causa, "erro.oraculo.indisponivel", detalhe);
    }
}
