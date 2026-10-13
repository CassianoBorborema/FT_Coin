package controller;

import dto.OraculoDTO;
import exception.AppException;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import model.ProvedorCotacao;

/**
 * Consulta de cotações do Oráculo. As cotações vêm da fonte configurada e não são cadastradas
 * pelo usuário.
 */
public class OraculoController {

    /** Maior período aceito na listagem de cotações. */
    static final int MAXIMO_DIAS_PERIODO = 366;

    private final ProvedorCotacao cotacoes;
    private final Clock relogio;

    /**
     * @param cotacoes fonte das cotações
     * @param relogio  relógio que define o dia de hoje
     */
    public OraculoController(ProvedorCotacao cotacoes, Clock relogio) {
        this.cotacoes = cotacoes;
        this.relogio = relogio;
    }

    public OraculoDTO cotacaoDeHoje() throws AppException {
        return consultar(LocalDate.now(relogio));
    }

    public OraculoDTO consultar(LocalDate data) throws AppException {
        if (data == null) {
            throw new AppException("Data da cotação é obrigatória.");
        }
        return new OraculoDTO(data, cotacoes.cotacaoEm(data));
    }

    /**
     * Lista as cotações de cada dia do período, inclusive o primeiro e o último.
     *
     * @param inicio primeiro dia do período
     * @param fim    último dia do período
     * @return as cotações em ordem cronológica
     * @throws AppException se o período for inválido, maior que {@value #MAXIMO_DIAS_PERIODO} dias
     *                      ou se faltar cotação em algum dia
     */
    public List<OraculoDTO> listarPeriodo(LocalDate inicio, LocalDate fim) throws AppException {
        if (inicio == null || fim == null) {
            throw new AppException("As datas de início e fim do período são obrigatórias.");
        }
        if (fim.isBefore(inicio)) {
            throw new AppException("A data final não pode ser anterior à data inicial.");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) >= MAXIMO_DIAS_PERIODO) {
            throw new AppException("O período deve ter no máximo " + MAXIMO_DIAS_PERIODO + " dias.");
        }
        List<OraculoDTO> lista = new ArrayList<>();
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            lista.add(consultar(data));
        }
        return lista;
    }
}
