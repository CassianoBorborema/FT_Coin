package testutil;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.slf4j.LoggerFactory;

/**
 * Captura os logs de uma classe durante o teste, no nível DEBUG, e restaura o logger ao fechar.
 */
public final class RegistroDeLogs implements AutoCloseable {

    private final Logger logger;
    private final ListAppender<ILoggingEvent> registros = new ListAppender<>();

    public RegistroDeLogs(Class<?> classe) {
        logger = (Logger) LoggerFactory.getLogger(classe);
        logger.setLevel(Level.DEBUG);
        registros.start();
        logger.addAppender(registros);
    }

    public List<ILoggingEvent> eventos() {
        return registros.list;
    }

    /** Níveis, mensagens e pilhas de exceção, como iriam para o arquivo de log. */
    public String texto() {
        StringBuilder texto = new StringBuilder();
        for (ILoggingEvent evento : registros.list) {
            texto.append(evento.getLevel()).append(' ').append(evento.getFormattedMessage()).append('\n');
            if (evento.getThrowableProxy() != null) {
                texto.append(ThrowableProxyUtil.asString(evento.getThrowableProxy()));
            }
        }
        return texto.toString();
    }

    @Override
    public void close() {
        logger.detachAppender(registros);
        logger.setLevel(null);
    }
}
