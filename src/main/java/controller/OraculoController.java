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
 * Consulta de cotações do Oráculo. O usuário não cadastra cotações.
 */
public class OraculoController {

    static final int MAXIMO_DIAS_PERIODO = 366;

    private final ProvedorCotacao cotacoes;
    private final Clock relogio;

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

    /** Cotações de cada dia entre {@code inicio} e {@code fim}, inclusive. */
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
