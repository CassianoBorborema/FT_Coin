package model;

import dto.OraculoDTO;
import exception.AppException;
import exception.PersistenciaException;
import java.time.LocalDate;

/**
 * Fornece a cotação da moeda virtual em uma data.
 * <p>
 * Isola o cálculo de apuração da origem da cotação (hoje o Oráculo),
 * o que permite trocar essa origem ou simulá-la em testes.
 * </p>
 */
@FunctionalInterface
public interface ProvedorCotacao {

    int DIAS_BUSCA_ULTIMA_COTACAO = 31;

    /**
     * Informa a cotação da moeda em uma data.
     *
     * @param data data da cotação desejada
     * @return a cotação da moeda na data informada
     * @throws AppException se não houver cotação para a data
     */
    double cotacaoEm(LocalDate data) throws AppException;

    /**
     * Procura a cotação mais recente até {@code data}, voltando no máximo
     * {@value #DIAS_BUSCA_ULTIMA_COTACAO} dias. Falhas de acesso ao banco não são tratadas como
     * "sem cotação no dia" e interrompem a busca.
     *
     * @throws AppException se não houver cotação no período
     */
    default OraculoDTO ultimaCotacaoAte(LocalDate data) throws AppException {
        for (int dias = 0; dias < DIAS_BUSCA_ULTIMA_COTACAO; dias++) {
            LocalDate dia = data.minusDays(dias);
            try {
                return new OraculoDTO(dia, cotacaoEm(dia));
            } catch (PersistenciaException e) {
                throw e;
            } catch (AppException _) {
                // sem cotação neste dia: tenta o anterior
            }
        }
        throw new AppException("erro.cotacao.semRecente", DIAS_BUSCA_ULTIMA_COTACAO, data);
    }
}
