package oraculo;

import client.OracleSimpleClient;
import config.Configuracao;
import exception.AppException;
import exception.OraculoIndisponivelException;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import model.ProvedorCotacao;
import network.ExchangeRate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cotação obtida do servidor do Oráculo pelo {@link OracleSimpleClient}.
 * <p>
 * Uma data sem cotação no servidor vira {@code AppException} comum, o que faz
 * {@link ProvedorCotacao#ultimaCotacaoAte} voltar um dia. Falha de conexão ou erro devolvido
 * pelo servidor vira {@link OraculoIndisponivelException}, que interrompe essa busca.
 * </p>
 */
public class Oraculo implements ProvedorCotacao {

    private static final Logger log = LoggerFactory.getLogger(Oraculo.class);

    private final OracleSimpleClient cliente;
    private final Clock relogio;

    public Oraculo(Configuracao configuracao, Clock relogio) throws AppException {
        this(new OracleSimpleClient(
                        configuracao.getTexto("oraculo.host", ""),
                        (int) configuracao.getInteiro("oraculo.porta"),
                        configuracao.getTexto("oraculo.grupo", ""),
                        configuracao.getTexto("oraculo.senha", "")),
                relogio);
    }

    Oraculo(OracleSimpleClient cliente, Clock relogio) {
        this.cliente = cliente;
        this.relogio = relogio;
    }

    @Override
    public double cotacaoEm(LocalDate data) throws AppException {
        if (data == null) {
            throw new AppException("erro.cotacao.semData");
        }
        if (data.isAfter(LocalDate.now(relogio))) {
            throw new AppException("erro.cotacao.futura", data);
        }

        log.debug("Consultando o Oráculo: {}", data);
        List<ExchangeRate> recebidas = buscar(data);
        for (ExchangeRate recebida : recebidas) {
            if (data.equals(recebida.getDate()) && recebida.getValue() != null) {
                model.Oraculo cotacao = new model.Oraculo(data, recebida.getValue().doubleValue());
                cotacao.validar();
                return cotacao.getCotacao();
            }
        }
        log.debug("O Oráculo não tem cotação em {}", data);
        throw new AppException("erro.cotacao.naoEncontrada", data);
    }

    private List<ExchangeRate> buscar(LocalDate data) throws AppException {
        try {
            List<ExchangeRate> recebidas = cliente.consultar(data, data);
            return Objects.requireNonNullElse(recebidas, List.of());
        } catch (Exception e) {
            log.warn("Oráculo indisponível ao consultar {}", data, e);
            throw new OraculoIndisponivelException(e, Objects.toString(e.getMessage(), e.getClass().getSimpleName()));
        }
    }
}
