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
            throw new AppException("erro.cotacao.semData");
        }
        return new OraculoDTO(data, cotacoes.cotacaoEm(data));
    }

    /** Cotações de cada dia entre {@code inicio} e {@code fim}, inclusive. */
    public List<OraculoDTO> listarPeriodo(LocalDate inicio, LocalDate fim) throws AppException {
        if (inicio == null || fim == null) {
            throw new AppException("erro.periodo.semDatas");
        }
        if (fim.isBefore(inicio)) {
            throw new AppException("erro.periodo.invertido");
        }
        if (ChronoUnit.DAYS.between(inicio, fim) >= MAXIMO_DIAS_PERIODO) {
            throw new AppException("erro.periodo.longo", MAXIMO_DIAS_PERIODO);
        }
        List<OraculoDTO> lista = new ArrayList<>();
        for (LocalDate data = inicio; !data.isAfter(fim); data = data.plusDays(1)) {
            lista.add(consultar(data));
        }
        return lista;
    }
}
