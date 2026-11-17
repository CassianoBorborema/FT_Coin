# FT_Coin

Sistema de apuração de ganhos e perdas em carteira de moedas virtuais, desenvolvido em **Java 25** com build **Maven**, arquitetura **MVC**, interface **CLI** e persistência via padrão **DAO** (em memória ou **MariaDB**).

---

## Estado atual do projeto


| Área                     | Status       | Observação                                                                         |
| ------------------------ | ------------ | ---------------------------------------------------------------------------------- |
| **Menu principal**       | Implementado | Carteira (1), Movimentação (2), Oráculo (3), Relatórios (4) e Ajuda (5) integrados |
| **Carteira (CRUD)**      | Implementado | Incluir, listar, editar e excluir com validações e confirmação de exclusão      |
| **Exclusão de carteira** | Implementado | Bloqueada quando existem movimentações vinculadas                                  |
| **Movimentação**         | Implementado | Compra, venda, crédito e saque na data do sistema; compra usa o saldo em reais     |
| **Oráculo**              | Implementado | Cotação de hoje, por data e por período, obtida do servidor do Oráculo             |
| **Relatórios**           | Implementado | Listagens, saldo (com impressão), histórico, evolução diária e resumo de ganho/perda |
| **Ajuda**                | Implementado | Como funciona, fluxos de carteira, movimentação e relatórios, e créditos          |
| **Persistência**         | Implementado | MariaDB; se o banco não estiver acessível, segue em memória                        |
| **Idiomas**              | Implementado | Português, inglês e espanhol, escolhidos ao iniciar                                |
| **Javadoc**              | Implementado | Todas as classes documentadas; o build do Javadoc falha em comentário mal escrito  |


A aplicação **compila e executa** todos os fluxos ponta a ponta.


| Documento                                                | Conteúdo                                              |
| -------------------------------------------------------- | ----------------------------------------------------- |
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md)         | Arquitetura, inicialização e cadastro de carteiras    |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra, venda, crédito e saque e regras de saldo      |
| [docs/fluxo-oraculo.md](docs/fluxo-oraculo.md)           | Cotação do servidor, histórico e consultas            |
| [docs/fluxo-relatorios.md](docs/fluxo-relatorios.md)     | Listagens, saldo, evolução diária e ganho/perda       |


---

## Estrutura do projeto

```
FT_Coin/
├── pom.xml                                  # Build Maven (Java 25, JUnit, Mockito, JaCoCo, MariaDB, Logback)
├── ftcoin.local.properties.example          # Modelo do ftcoin.local.properties (credenciais, fora do Git)
├── docs/
│   ├── README.md
│   ├── fluxo-carteira.md
│   ├── fluxo-movimentacao.md
│   ├── fluxo-oraculo.md
│   └── fluxo-relatorios.md
└── src/
    ├── main/
    │   ├── java/
    │   │   ├── app/
    │   │   │   └── Main.java                # Monta as dependências e abre o menu principal
    │   │   ├── config/
    │   │   │   ├── Configuracao.java        # Leitura do ftcoin.properties
    │   │   │   └── Mensagens.java           # Textos no idioma escolhido (i18n)
    │   │   ├── model/
    │   │   │   ├── Carteira.java
    │   │   │   ├── Movimentacao.java
    │   │   │   ├── TipoMovimentacao.java    # Compra, venda, crédito e saque aplicados à posição (polimorfismo)
    │   │   │   ├── Oraculo.java
    │   │   │   ├── Posicao.java             # Quantidade, custo médio, totais e saldo em reais
    │   │   │   ├── ApuracaoCarteira.java    # Histórico e resultados da carteira
    │   │   │   └── ProvedorCotacao.java     # Contrato de cotação por data
    │   │   ├── oraculo/
    │   │   │   ├── Oraculo.java       # Cotação obtida do servidor do Oráculo
    │   │   │   └── CotacaoComHistorico.java # Registra cada cotação obtida no OraculoDAO
    │   │   ├── dto/
    │   │   │   ├── CarteiraDTO.java
    │   │   │   ├── MovimentacaoDTO.java
    │   │   │   ├── OraculoDTO.java
    │   │   │   └── ApuracaoDTO.java         # Record com as linhas do histórico e os dias aninhados
    │   │   ├── dao/
    │   │   │   ├── CarteiraDAO.java         # Interfaces seladas (sealed)
    │   │   │   ├── MovimentacaoDAO.java
    │   │   │   ├── OraculoDAO.java
    │   │   │   ├── *DAOMemoria.java         # Implementações em memória
    │   │   │   ├── *DAOMariaDB.java         # Implementações em MariaDB
    │   │   │   ├── FabricaConexao.java      # Conexões JDBC, transações e criação das tabelas
    │   │   │   └── Persistencia.java        # Escolhe e agrupa os DAOs (memória ou MariaDB)
    │   │   ├── controller/
    │   │   │   ├── CarteiraController.java
    │   │   │   ├── MovimentacaoController.java
    │   │   │   ├── OraculoController.java
    │   │   │   └── RelatorioController.java
    │   │   ├── view/
    │   │   │   ├── Console.java             # Entrada e saída de texto
    │   │   │   ├── Menu.java                # Base dos menus (opções, laço e erros)
    │   │   │   ├── Formatador.java          # Datas, moeda e quantidades
    │   │   │   ├── MenuPrincipal.java
    │   │   │   ├── MenuCarteira.java
    │   │   │   ├── MenuMovimentacao.java
    │   │   │   ├── MenuOraculo.java
    │   │   │   ├── MenuRelatorios.java
    │   │   │   └── MenuAjuda.java
    │   │   └── exception/
    │   │       ├── AppException.java        # Chave da mensagem + argumentos, traduzida ao ser lida
    │   │       └── PersistenciaException.java
    │   └── resources/
    │       ├── ftcoin.properties            # Configuração padrão, sem credenciais (idioma, persistência, banco, crédito e oráculo)
│       ├── logback.xml                  # Logs em arquivo com rotação
    │       ├── db/
    │       │   └── schema.sql               # Tabelas do MariaDB
    │       ├── i18n/
    │       │   └── mensagens_{pt,en,es}.properties  # Menus e mensagens em cada idioma
    │       └── textos/{pt,en,es}/           # Ajuda, fluxos (carteira, movimentação, relatórios) e créditos
    └── test/
        └── java/
            ├── config/                                  # Configuração e traduções (mesmas chaves nos 3 idiomas)
            ├── model/
            │   ├── ApuracaoCarteiraTest.java
            │   └── ApuracaoCarteiraPlanilhaTest.java   # Reproduz a planilha de apuração de referência
            ├── controller/                              # Controllers com DAOs em memória
            ├── dao/                                     # Contrato dos DAOs (memória e MariaDB em contêiner) e conexão
            ├── oraculo/                                 # Oráculo (servidor), cliente de rede e histórico
            ├── view/                                    # Menu e Console
            └── testutil/                                # Cotações fixas e relógio parado para testes
```

### Responsabilidade por camada


| Camada         | Pacote                      | Papel                                               |
| -------------- | --------------------------- | --------------------------------------------------- |
| **Model**      | `model`                     | Entidades de domínio, validações e apuração         |
| **DTO**        | `dto`                       | Transporte entre view, controller e DAO             |
| **DAO**        | `dao`                       | Interfaces seladas e persistência em memória e MariaDB |
| **Controller** | `controller`                | Regras de negócio e orquestração                    |
| **View**       | `view`                      | Menus CLI, entrada/saída e formatação               |
| **Oráculo**    | `oraculo`, `client`, `network` | Cotação do servidor, cliente de rede e histórico |
| **Config**     | `config`                    | Configuração (`ftcoin.properties`) e idioma (`Mensagens`) |
| **Exception**  | `exception`                 | Erros de aplicação centralizados                    |


### Modelo de dados (referência)


| Tabela           | Origem                                  | Campos principais                                                                          |
| ---------------- | --------------------------------------- | ------------------------------------------------------------------------------------------ |
| **CARTEIRA**     | Já existia no banco (oficial, 2022)     | `IdCarteira` (PK), `Titular` (até 50), `Corretora` (até 50)                                |
| **MOVIMENTACAO** | Já existia no banco (oficial, 2022)     | `IdMovimento` (PK), `IdCarteira` (FK), `Data`, `TipoOperacao` (`C`/`V`/`D`/`S`), `Quantidade` (até 3 casas, menor que 10.000.000) |
| **COTACAO**      | Criada pelo projeto                     | `Data` (PK), `Valor`                                                                       |

Tabelas criadas pelo projeto têm o nome em maiúsculas. O servidor diferencia maiúsculas de minúsculas,
então o SQL usa os nomes exatos. A aplicação só executa `CREATE TABLE IF NOT EXISTS` (nunca `ALTER` nem
`DROP`): no banco do grupo, `CARTEIRA` e `MOVIMENTACAO` já existem e só `COTACAO` é criada. Num banco vazio,
o script cria as três. Os limites de tamanho e de casas decimais são validados no modelo, para a memória e o
banco aceitarem os mesmos valores.

O script completo está em [schema.sql](src/main/resources/db/schema.sql). A especificação da mudança está em
[docs/spec-banco.md](docs/spec-banco.md).


---

## Requisitos do sistema (escopo)

- **Interface:** CLI com menu principal (Carteira, Movimentação, Oráculo, Relatórios, Ajuda, Sair) e submenus.
- **Carteira:** incluir, listar, editar e excluir.
- **Movimentação:** compra e venda de moeda virtual, crédito e saque em reais.
- **Oráculo:** consultar cotações diárias, usadas nas movimentações e nos cálculos. As cotações vêm do servidor do Oráculo (`oraculo.host` e `oraculo.porta`); sem conexão, o menu informa a indisponibilidade.
- **Relatórios:** listagens ordenadas, saldo, histórico e ganho/perda por carteira, valorizados a custo médio e a mercado.
- **Persistência:** padrão DAO com implementações **em memória** e **MariaDB**; tudo funciona nas duas.
- **Idiomas:** português, inglês e espanhol, escolhidos ao iniciar.
- **Documentação:** Javadoc em todas as classes.
- **Boas práticas:** POO, encapsulamento, polimorfismo, validações, exceções, compilação sem erros.

---

## Compilação e execução

**Pré-requisitos:** JDK 25 ou superior e [Maven](https://maven.apache.org/download.cgi) 3.9+ no PATH (ou a IDE, que já traz o Maven embutido).

Na raiz do repositório:

```bash
mvn package                                 # compila, roda os testes e gera o jar
java -jar target/ft-coin-1.0.0-SNAPSHOT.jar
```

Outros comandos úteis:

| Comando                  | O que faz                                  |
| ------------------------ | ------------------------------------------ |
| `mvn test`               | Roda só os testes                          |
| `mvn verify`             | Testes + relatório de cobertura do JaCoCo em `target/site/jacoco/index.html` |
| `mvn javadoc:javadoc`    | Gera o Javadoc em `target/reports/apidocs`; falha se algum comentário estiver mal escrito |
| `mvn clean`              | Apaga a pasta `target/`                    |

> **IDE:** Eclipse (*Import → Existing Maven Projects*), IntelliJ e VS Code abrem o `pom.xml` diretamente. Configure a IDE com um JDK 25+; se ela acusar erros como "`_` is a keyword", recarregue o projeto Maven.

> **Testes com MariaDB:** os testes dos DAOs do banco sobem um MariaDB em contêiner (Testcontainers) e
> precisam do Docker em execução. Sem Docker, esses testes são ignorados e o restante roda normalmente.

### Usando o MariaDB

Ao iniciar, a aplicação tenta conectar ao MariaDB. Se conseguir, cria as tabelas que faltarem (no banco do grupo, só `COTACAO`) e grava
tudo no banco. Se não conseguir (banco fora do ar, senha errada ou ausente), mostra o motivo e segue
com a **persistência em memória**, em que os dados são perdidos ao sair.

Para que a conexão funcione:

1. Tenha um MariaDB com um banco e um usuário para a aplicação. Com Docker, por exemplo:

   ```bash
   docker run -d --name ftcoin-db -p 3306:3306 \
     -e MARIADB_DATABASE=ftcoin -e MARIADB_USER=ftcoin \
     -e MARIADB_PASSWORD=<senha> -e MARIADB_RANDOM_ROOT_PASSWORD=1 \
     mariadb:11.4
   ```

2. Informe a senha, sem colocá-la no `ftcoin.properties` versionado. Qualquer uma das formas serve
   (a ordem de precedência está em [Configuração local](#configuração-local)):

   | Forma                          | Exemplo                                     |
   | ------------------------------ | ------------------------------------------- |
   | Variável de ambiente           | `FTCOIN_DB_SENHA=<senha>`                   |
   | Propriedade na execução        | `java -Ddb.senha=<senha> -jar ...`          |
   | Arquivo `ftcoin.local.properties` no diretório de execução (já ignorado pelo Git) | `db.senha=<senha>` |

   URL e usuário padrão: `jdbc:mariadb://localhost:3306/ftcoin` e `ftcoin` (chaves `db.url` e `db.usuario`).

3. Rode normalmente. A primeira linha mostra qual persistência está em uso.

### Configuração local

O `ftcoin.properties` versionado traz só os valores padrão, sem credenciais. Endereço do banco, senha e dados
do Oráculo ficam no **`ftcoin.local.properties`**, na pasta de onde a aplicação é executada. O arquivo é
ignorado pelo Git. Para criá-lo, copie o modelo e preencha:

```bash
cp ftcoin.local.properties.example ftcoin.local.properties
```

Cada chave é procurada nesta ordem: propriedade `-D`, variável de ambiente `FTCOIN_<CHAVE>`, o
`ftcoin.local.properties` e, por fim, o `ftcoin.properties`.

### Logs

A aplicação registra eventos em `logs/ftcoin.log` (pasta ignorada pelo Git), com Logback via SLF4J. Cada linha
tem data, hora, nível, classe e mensagem; erros trazem a pilha da exceção. O arquivo é trocado a cada 1 MB e
são guardados até 5 anteriores (`logs/ftcoin.1.log` a `ftcoin.5.log`). O console fica livre para os menus.

| Nível | O que registra |
| ----- | -------------- |
| `INFO` | Início e fim, persistência em uso, tabelas verificadas, carteira incluída, editada ou excluída, movimentação registrada e cotação gravada no histórico |
| `WARN` | MariaDB indisponível, Oráculo indisponível e operações recusadas por regra de negócio (só a chave da mensagem) |
| `ERROR` | Falha em comando do banco, erro inesperado em um menu e falha ao iniciar |
| `DEBUG` | Consultas ao Oráculo e cotações lidas do histórico |

O nível padrão é `INFO`. Para ver `DEBUG`, use `FTCOIN_LOG_NIVEL=DEBUG` ou `-DFTCOIN_LOG_NIVEL=DEBUG`.
Senhas e nomes de titulares não são registrados. A configuração está em
[logback.xml](src/main/resources/logback.xml); os testes usam um `logback-test.xml` silencioso.

### Idioma

Ao iniciar, a aplicação pergunta o idioma (1. Português, 2. English, 3. Español). Para pular a pergunta,
defina `idioma` como `pt`, `en` ou `es` por qualquer uma das formas acima (por exemplo `-Didioma=en`).
O idioma também define o formato das datas (`dd/MM/yyyy` em português e espanhol, `MM/dd/yyyy` em
inglês) e dos números. Os valores são sempre em reais.

### Somente memória

Para usar a memória direto, sem tentar o banco (por exemplo, em uma máquina sem MariaDB, evitando
esperar o tempo de conexão), use `persistencia=memoria` por qualquer uma das formas acima
(`FTCOIN_PERSISTENCIA=memoria`, `-Dpersistencia=memoria` ou no `ftcoin.local.properties`).

---

## Teste rápido

### Carteira

1. Menu **1 (Carteira)** → **1 (Incluir)** → titular e corretora
2. **2 (Listar carteiras)** → ver todas as carteiras → **0 (Voltar)**

> Onde uma carteira é necessária, o sistema lista as carteiras (titular e corretora) e você escolhe
> pelo número da lista; não é preciso saber o identificador.

### Movimentação

1. Menu **2 (Movimentação)** → **3 (Crédito em reais)** → escolher a carteira → valor (ex.: 200)
2. **1 (Compra)** → escolher a carteira → conferir a cotação de hoje e o saldo em reais → quantidade (ex.: 10)
3. **2 (Venda)** → mesma carteira, quantidade ≤ saldo (ex.: 5); o valor da venda volta para o saldo em reais
4. **4 (Saque em reais)** → valor ≤ saldo em reais
5. Tentar compra sem saldo em reais ou venda acima do saldo de FTCoins → erro esperado
6. Menu **1 (Carteira)** → **4 (Excluir)** carteira com movimentos → bloqueio

> A regra do saldo em reais vale para compras a partir de `credito.vigencia` (em `ftcoin.properties`).
> Compras anteriores a essa data não mexem no saldo em reais.

### Oráculo

1. Menu **3 (Oráculo)** → **1 (Cotação de hoje)**
2. **3 (Listar por período)** → ex.: `01/10/2026` a `05/10/2026` → **0 (Voltar)**

### Relatórios

1. Menu **4 (Relatórios)** → **1/2** listar carteiras por ordem de cadastro/titular
2. **3 (Saldo atual)** → escolher a carteira → responder **S** para imprimir o saldo em `saldo-carteira-<id>-<data>.txt`
3. **4 (Histórico)** e **5 (Evolução diária)** → escolher a carteira
4. **6 (Ganhos e perdas)** → resumo de cada carteira no formato da planilha de apuração, a custo médio e a mercado (última cotação)

### Oráculo

As cotações são pedidas ao servidor do Oráculo pelo cliente em `src/main/java/client/OracleSimpleClient.java`.
Endereço, grupo e senha vêm das chaves `oraculo.host`, `oraculo.porta`, `oraculo.grupo` e `oraculo.senha`,
que não têm valor no `ftcoin.properties` versionado. Preencha-as no `ftcoin.local.properties` (veja
[Configuração local](#configuração-local)). O mesmo cliente pode ser rodado sozinho para testar a conexão:
`java -cp target/classes client.OracleSimpleClient`.

Datas futuras não são consultadas no servidor. Uma data sem cotação volta para o dia anterior, até 31 dias,
como na busca da última cotação. Falha de conexão ou erro do servidor é mostrado ao usuário sem essa busca.
Detalhes em [docs/fluxo-oraculo.md](docs/fluxo-oraculo.md).

---

## Documentação


| Documento                                                | Descrição                                        |
| -------------------------------------------------------- | ------------------------------------------------ |
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md)         | Arquitetura, inicialização e cadastro            |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra, venda, crédito, saque e regras de saldo  |
| [docs/fluxo-oraculo.md](docs/fluxo-oraculo.md)           | Cotação do servidor, histórico e consultas       |
| [docs/fluxo-relatorios.md](docs/fluxo-relatorios.md)     | Listagens, saldo, evolução diária e ganho/perda  |
| [docs/README.md](docs/README.md)                         | Índice da pasta de documentação                  |
| Javadoc (`mvn javadoc:javadoc`)                          | Referência das classes                           |


---

## Controle de versão

Trabalho em equipe com Git: cada funcionalidade ou camada em branch ou commits pequenos facilita revisão e o relatório de contribuição.