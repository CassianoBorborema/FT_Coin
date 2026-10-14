package dao;

import dto.MovimentacaoDTO;
import exception.AppException;
import java.util.List;

/**
 * Persistência das movimentações. Movimentações não são alteradas nem excluídas.
 */
public sealed interface MovimentacaoDAO permits MovimentacaoDAOMemoria, MovimentacaoDAOMariaDB {

    /**
     * Com identificador zero, um identificador novo, maior que todos os anteriores, é gerado e
     * gravado de volta em {@code movimentacao}.
     *
     * @throws AppException se o identificador informado já estiver em uso
     */
    void incluir(MovimentacaoDTO movimentacao) throws AppException;

    /** @throws AppException se a movimentação não existir */
    MovimentacaoDTO consultarPorId(int idMovimento) throws AppException;

    /** @return as movimentações da carteira, em qualquer ordem */
    List<MovimentacaoDTO> listarPorCarteira(int idCarteira) throws AppException;

    boolean possuiMovimentacoes(int idCarteira) throws AppException;
}
