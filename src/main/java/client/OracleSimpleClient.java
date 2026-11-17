/*
 * ************************************************************************************************************************
 * Prof. Dr. André F. de Angelis
 * Unicamp - Universidade Estadual de Campinas
 * FT - Faculdade de Tecnologia
 * ************************************************************************************************************************
 * Escrito em Java 20, usando IDE Eclipse 2023-06, em ambiente Windows 11
 *
 * Versão 1.0
 * 07.Outubro.2026
 *
 * ************************************************************************************************************************
 *
 * Este programa é o módulo cliente de um sistema cliente/servidor para demonstração do uso de fluxos (streams) de objetos
 * enviados e recebidos pela rede, com uso de uma API proprietária para acesso ao servidor.
 *
 * Este módulo prepara uma requisição de cotações de moeda, informando a feixa de datas das cotações desejadas e a envia
 * para o servidor, que responde com uma lista (eventualmente vazia) de objetos do tipo ExchangeRate.
 *
 * Este cliente será usado temporariamente pelos alunos para testes de conexão e API; posteriormente, suas funcionalidades
 * serão incorporadas, com devidas adaptações, ao projeto FTCoin, ver. 2026.
 *
 * ************************************************************************************************************************
 */

package client;

import config.Configuracao;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

import network.ExchangeRate;
import network.NetworkRequest;
import network.NetworkResponse;

public final class OracleSimpleClient
   {
   private final String        host;
   private final int           port;
   private final String        groupName;
   private final String        groupPassword;

   public OracleSimpleClient(String host, int port, String groupName, String groupPassword)
      {
      this.host          = host;
      this.port          = port;
      this.groupName     = groupName;
      this.groupPassword = groupPassword;
      }

   public static void main(String[] args)
      {
      // data range to be requested
      final LocalDate lastDate   = LocalDate.now();
      final LocalDate firstDate  = lastDate.minusDays(7);

      try
         {
         final Configuracao configuracao = Configuracao.carregar();
         final OracleSimpleClient client = new OracleSimpleClient(
               configuracao.getTexto("oraculo.host"),
               (int) configuracao.getInteiro("oraculo.porta"),
               configuracao.getTexto("oraculo.grupo"),
               configuracao.getTexto("oraculo.senha"));
         client.go(firstDate, lastDate);

         }
      catch (final Exception exception)
         {
         System.err.println("Client encountered error: " + exception.getMessage());
         System.exit(1);
         }
      }

   public List<ExchangeRate> consultar(LocalDate since, LocalDate to) throws Exception
      {
      return getValues(since, to);
      }

   private List<ExchangeRate> getValues(LocalDate since, LocalDate to) throws Exception
      {
      final NetworkRequest  request  = new NetworkRequest(groupName, groupPassword, since, to);
      final NetworkResponse response = sendRequest(request);

      if (response.errorMessage != null)
         {
         throw new RuntimeException(response.errorMessage);
         }

      return (response.getExchangeRates());
      }

   private void go(LocalDate firstDate, LocalDate lastDate) throws Exception
      {
      printList(getValues(firstDate, lastDate));
      }

   private void printList(List<ExchangeRate> records)
      {
      if (!records.isEmpty())
         {
         for (final ExchangeRate record : records)
            {
            System.out.printf("%s, %.2f\n", record.getDate(), record.getValue());
            }
         }
      else
         {
         System.out.printf("No records found");
         }
      }

   private NetworkResponse sendRequest(NetworkRequest request) throws Exception
      {
      try (Socket socket = new Socket(host, port); ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream()); ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream()))
         {
         outputStream.writeObject(request);
         outputStream.flush();
         return ((NetworkResponse) inputStream.readObject());
         }
      }
   }
