package oraculo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import client.OracleSimpleClient;
import dto.OraculoDTO;
import exception.AppException;
import exception.OraculoIndisponivelException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import testutil.CotacoesFixas;

class OraculoTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 1, 30);
    private static final String GRUPO = "GRUPO";
    private static final String SENHA = "SENHA";

    private final List<NetworkRequest> pedidos = new CopyOnWriteArrayList<>();
    private ServerSocket servidor;

    @AfterEach
    void fecharServidor() throws IOException {
        if (servidor != null) {
            servidor.close();
        }
    }

    @Test
    void cotacaoDaDataPedidaVemDoServidor() throws Exception {
        Oraculo oraculo = servidorQue(pedido -> new NetworkResponse(
                List.of(new ExchangeRate(HOJE, new BigDecimal("12.34"))), null));

        assertEquals(12.34, oraculo.cotacaoEm(HOJE));
        NetworkRequest pedido = pedidos.get(0);
        assertEquals(GRUPO, pedido.getGroupName());
        assertEquals(SENHA, pedido.getGroupPassword());
        assertEquals(HOJE, pedido.getFirstDate());
        assertEquals(HOJE, pedido.getLastDate());
    }

    @Test
    void diaSemCotacaoNoServidorEhFalhaComumDeNegocio() throws Exception {
        Oraculo oraculo = servidorQue(pedido -> new NetworkResponse(List.of(), null));

        AppException erro = assertThrows(AppException.class, () -> oraculo.cotacaoEm(HOJE));
        assertEquals("erro.cotacao.naoEncontrada", erro.getChave());
        assertFalse(erro instanceof OraculoIndisponivelException);
    }

    @Test
    void ultimaCotacaoAteVoltaOsDiasSemCotacao() throws Exception {
        Oraculo oraculo = servidorQue(pedido -> pedido.getFirstDate().equals(HOJE.minusDays(2))
                ? new NetworkResponse(List.of(new ExchangeRate(HOJE.minusDays(2), new BigDecimal("10.5"))), null)
                : new NetworkResponse(List.of(), null));

        OraculoDTO cotacao = oraculo.ultimaCotacaoAte(HOJE);

        assertEquals(HOJE.minusDays(2), cotacao.getData());
        assertEquals(10.5, cotacao.getCotacao());
    }

    @Test
    void erroDevolvidoPeloServidorEhIndisponivel() throws Exception {
        Oraculo oraculo = servidorQue(pedido -> new NetworkResponse(null, "Senha inválida"));

        assertThrows(OraculoIndisponivelException.class, () -> oraculo.cotacaoEm(HOJE));
    }

    @Test
    void servidorInacessivelEhIndisponivelENaoViraSemCotacao() throws IOException {
        int porta;
        try (ServerSocket livre = new ServerSocket(0, 50, InetAddress.getLoopbackAddress())) {
            porta = livre.getLocalPort();
        }
        Oraculo oraculo = new Oraculo(
                new OracleSimpleClient("127.0.0.1", porta, GRUPO, SENHA), CotacoesFixas.relogioEm(HOJE));

        assertThrows(OraculoIndisponivelException.class, () -> oraculo.ultimaCotacaoAte(HOJE));
    }

    @Test
    void dataFuturaNaoConsultaOServidor() {
        Oraculo oraculo = new Oraculo(
                new OracleSimpleClient("127.0.0.1", 1, GRUPO, SENHA), CotacoesFixas.relogioEm(HOJE));

        AppException erro = assertThrows(AppException.class, () -> oraculo.cotacaoEm(HOJE.plusDays(1)));
        assertEquals("erro.cotacao.futura", erro.getChave());
    }

    private Oraculo servidorQue(Function<NetworkRequest, NetworkResponse> resposta) throws IOException {
        servidor = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        ServerSocket atual = servidor;
        Thread.ofVirtual().start(() -> atender(atual, resposta));
        return new Oraculo(
                new OracleSimpleClient("127.0.0.1", atual.getLocalPort(), GRUPO, SENHA),
                CotacoesFixas.relogioEm(HOJE));
    }

    private void atender(ServerSocket atual, Function<NetworkRequest, NetworkResponse> resposta) {
        while (!atual.isClosed()) {
            try (Socket conexao = atual.accept();
                 ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream());
                 ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())) {
                NetworkRequest pedido = (NetworkRequest) entrada.readObject();
                pedidos.add(pedido);
                saida.writeObject(resposta.apply(pedido));
                saida.flush();
            } catch (IOException | ClassNotFoundException e) {
                // conexão encerrada antes do fim do teste: aguarda a próxima
            }
        }
    }
}
