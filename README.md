# FT_Coin

Sistema de apuração de ganhos e perdas em carteira de moedas virtuais, desenvolvido em **Java (17+)** com build **Maven** com arquitetura **MVC**, interface **CLI** e persistência via padrão **DAO** (em memória ou **MariaDB**).

---

## Estado atual do projeto


| Área                     | Status       | Observação                                                                         |
| ------------------------ | ------------ | ---------------------------------------------------------------------------------- |
| **Menu principal**       | Implementado | Carteira (1), Movimentação (2), Oráculo (3), Relatórios (4) e Ajuda (5) integrados |
| **Carteira (CRUD)**      | Implementado | Incluir, consultar, editar e excluir com validações e confirmação de exclusão      |
| **Exclusão de carteira** | Implementado | Bloqueada quando existem movimentações vinculadas                                  |
| **Movimentação**         | Implementado | Compra e venda na data do sistema, à cotação do dia, com validação de saldo        |
| **Oráculo**              | Simulado     | Cotação de hoje, por data e por período, gerada pela fórmula da planilha           |
| **Relatórios**           | Implementado | Listagens (id/titular), saldo, histórico e ganho/perda (custo médio e mercado)  |
| **Ajuda**                | Implementado | Texto "Como funciona" (guia de uso)                                                |
| **Persistência**         | Implementado | MariaDB; se o banco não estiver acessível, segue em memória                        |


A aplicação **compila e executa** todos os fluxos ponta a ponta.


| Documento                                                | Conteúdo                                              |
| -------------------------------------------------------- | ----------------------------------------------------- |
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md)         | Arquitetura, menus, CRUD e interação da carteira      |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra/venda, Oráculo, saldo e diagramas de sequência |
| [docs/fluxo-oraculo.md](docs/fluxo-oraculo.md)           | Consulta e listagem de cotações                       |
| [docs/fluxo-relatorios.md](docs/fluxo-relatorios.md)     | Listagens, saldo, histórico e cálculo de ganho/perda  |


---

## Estrutura do projeto

```
FT_Coin/
├── pom.xml                                  # Build Maven (Java 17+, JUnit 5, driver MariaDB)
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
    │   │   │   └── Configuracao.java        # Leitura do ftcoin.properties
    │   │   ├── model/
    │   │   │   ├── Carteira.java
    │   │   │   ├── Movimentacao.java
    │   │   │   ├── TipoMovimentacao.java    # Compra/venda aplicadas à posição (polimorfismo)
    │   │   │   ├── Oraculo.java
    │   │   │   ├── Posicao.java             # Quantidade, custo médio e totais da carteira
    │   │   │   ├── ApuracaoCarteira.java    # Histórico e resultados da carteira
    │   │   │   └── ProvedorCotacao.java     # Contrato de cotação por data
    │   │   ├── oraculo/
    │   │   │   ├── OraculoSimulado.java     # Cotação pela fórmula da planilha
    │   │   │   ├── ParametrosOraculoSimulado.java
    │   │   │   └── CotacaoComHistorico.java # Registra cada cotação obtida no OraculoDAO
    │   │   ├── dto/
    │   │   │   ├── CarteiraDTO.java
    │   │   │   ├── MovimentacaoDTO.java
    │   │   │   ├── OraculoDTO.java
    │   │   │   ├── ApuracaoDTO.java
    │   │   │   └── LinhaApuracaoDTO.java
    │   │   ├── dao/
    │   │   │   ├── CarteiraDAO.java
    │   │   │   ├── MovimentacaoDAO.java
    │   │   │   ├── OraculoDAO.java
    │   │   │   ├── FabricaDAO.java          # Cria os DAOs de uma persistência
    │   │   │   ├── memoria/
    │   │   │   │   ├── FabricaDAOMemoria.java
    │   │   │   │   ├── CarteiraDAOMemoria.java
    │   │   │   │   ├── MovimentacaoDAOMemoria.java
    │   │   │   │   └── OraculoDAOMemoria.java
    │   │   │   └── mariadb/
    │   │   │       ├── FabricaDAOMariaDB.java
    │   │   │       ├── CarteiraDAOMariaDB.java
    │   │   │       ├── MovimentacaoDAOMariaDB.java
    │   │   │       └── OraculoDAOMariaDB.java
    │   │   ├── infra/db/
    │   │   │   ├── ConfiguracaoBanco.java   # URL, usuário, senha e timeout
    │   │   │   ├── FabricaConexao.java      # Conexões JDBC e transações
    │   │   │   └── InicializadorBanco.java  # Cria as tabelas na inicialização
    │   │   ├── controller/
    │   │   │   ├── CarteiraController.java
    │   │   │   ├── MovimentacaoController.java
    │   │   │   ├── OraculoController.java
    │   │   │   └── RelatorioController.java
    │   │   ├── view/
    │   │   │   ├── Console.java             # Entrada e saída de texto
    │   │   │   ├── Menu.java                # Base dos menus (opções, laço e erros)
    │   │   │   ├── OpcaoMenu.java
    │   │   │   ├── Formatador.java          # Datas, moeda e quantidades
    │   │   │   ├── MenuPrincipal.java
    │   │   │   ├── MenuCarteira.java
    │   │   │   ├── MenuMovimentacao.java
    │   │   │   ├── MenuOraculo.java
    │   │   │   ├── MenuRelatorios.java
    │   │   │   └── MenuAjuda.java
    │   │   └── exception/
    │   │       ├── AppException.java
    │   │       └── PersistenciaException.java
    │   └── resources/
    │       ├── ftcoin.properties            # Configuração (persistência, banco e oráculo)
    │       ├── db/
    │       │   └── schema.sql               # Tabelas do MariaDB
    │       └── textos/
    │           ├── ajuda.txt                # Texto longo da tela de ajuda
    │           └── creditos.txt             # Tela de créditos
    └── test/
        └── java/
            ├── model/
            │   ├── ApuracaoCarteiraTest.java
            │   └── ApuracaoCarteiraPlanilhaTest.java   # Reproduz a planilha de apuração de referência
            ├── controller/                              # Controllers com DAOs em memória
            ├── dao/                                     # Contrato dos DAOs (memória e MariaDB em contêiner)
            ├── infra/db/                                # Configuração e conexão com o banco
            ├── oraculo/                                 # Oráculo simulado e histórico de cotações
            ├── view/                                    # Menu e Console
            └── testutil/                                # Cotações fixas e relógio parado para testes
```

### Responsabilidade por camada


| Camada         | Pacote                      | Papel                                               |
| -------------- | --------------------------- | --------------------------------------------------- |
| **Model**      | `model`                     | Entidades de domínio, validações e apuração         |
| **DTO**        | `dto`                       | Transporte entre view, controller e DAO             |
| **DAO**        | `dao`, `dao.memoria`, `dao.mariadb` | Contratos e persistência em memória e MariaDB |
| **Infra**      | `infra.db`                  | Conexão JDBC, transações e criação das tabelas      |
| **Controller** | `controller`                | Regras de negócio e orquestração                    |
| **View**       | `view`                      | Menus CLI, entrada/saída e formatação               |
| **Oráculo**    | `oraculo`                   | Fontes de cotação (simulada) e histórico            |
| **Config**     | `config`                    | Configuração da aplicação (`ftcoin.properties`)     |
| **Exception**  | `exception`                 | Erros de aplicação centralizados                    |


### Modelo de dados (referência)


| Tabela           | Campos principais                                                               |
| ---------------- | ------------------------------------------------------------------------------- |
| **carteira**     | `id` (PK), `nome_titular`, `corretora`                                          |
| **movimentacao** | `id` (PK), `id_carteira` (FK), `data`, `tipo` (`C`/`V`), `quantidade`           |
| **cotacao**      | `data` (PK), `valor`                                                            |

O script completo está em [schema.sql](src/main/resources/db/schema.sql).


---

## Requisitos do sistema (escopo)

- **Interface:** CLI com menu principal (Carteira, Movimentação, Oráculo, Relatórios, Ajuda, Sair) e submenus.
- **Carteira:** incluir, consultar, editar e excluir.
- **Movimentação:** compra e venda de moeda virtual.
- **Oráculo:** consultar cotações diárias, usadas nas movimentações e nos cálculos. Até a API existir, as cotações são simuladas pela fórmula da planilha de referência.
- **Relatórios:** listagens ordenadas, saldo, histórico e ganho/perda por carteira, valorizados a custo médio e a mercado.
- **Persistência:** padrão DAO com implementações **em memória** e **MariaDB**.
- **Boas práticas:** POO, encapsulamento, polimorfismo, validações, exceções, compilação sem erros.

---

## Compilação e execução

**Pré-requisitos:** JDK 17 ou superior e [Maven](https://maven.apache.org/download.cgi) 3.9+ no PATH (ou a IDE, que já traz o Maven embutido).

Na raiz do repositório:

```bash
mvn package                                 # compila, roda os testes e gera o jar
java -jar target/ft-coin-1.0.0-SNAPSHOT.jar
```

Outros comandos úteis:

| Comando                  | O que faz                                  |
| ------------------------ | ------------------------------------------ |
| `mvn test`               | Roda só os testes                          |
| `mvn javadoc:javadoc`    | Gera o Javadoc em `target/reports/apidocs` |
| `mvn clean`              | Apaga a pasta `target/`                    |

> **IDE:** Eclipse (*Import → Existing Maven Projects*), IntelliJ e VS Code abrem o `pom.xml` diretamente.

> **Testes com MariaDB:** os testes dos DAOs do banco sobem um MariaDB em contêiner (Testcontainers) e
> precisam do Docker em execução. Sem Docker, esses testes são ignorados e o restante roda normalmente.

### Usando o MariaDB

Ao iniciar, a aplicação tenta conectar ao MariaDB. Se conseguir, cria as tabelas que faltarem e grava
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

2. Informe a senha, sem colocá-la no `ftcoin.properties` versionado. Qualquer uma das formas serve:

   | Forma                          | Exemplo                                     |
   | ------------------------------ | ------------------------------------------- |
   | Variável de ambiente           | `FTCOIN_DB_SENHA=<senha>`                   |
   | Propriedade na execução        | `java -Ddb.senha=<senha> -jar ...`          |
   | Arquivo `ftcoin.local.properties` no diretório de execução (já ignorado pelo Git) | `db.senha=<senha>` |

   URL e usuário padrão: `jdbc:mariadb://localhost:3306/ftcoin` e `ftcoin` (chaves `db.url` e `db.usuario`).

3. Rode normalmente. A primeira linha mostra qual persistência está em uso.

Para usar a memória direto, sem tentar o banco (por exemplo, em uma máquina sem MariaDB, evitando
esperar o tempo de conexão), use `persistencia=memoria` por qualquer uma das formas acima
(`FTCOIN_PERSISTENCIA=memoria`, `-Dpersistencia=memoria` ou no `ftcoin.local.properties`).

---

## Teste rápido

### Carteira

1. Menu **1 (Carteira)** → **1 (Incluir)** → titular e corretora → anotar ID
2. **2 (Consultar)** → informar ID → **0 (Voltar)**

### Movimentação

1. Menu **2 (Movimentação)** → **1 (Compra)** → ID da carteira → conferir a cotação de hoje → quantidade (ex.: 10)
2. **2 (Venda)** → mesma carteira, quantidade ≤ saldo (ex.: 5)
3. Tentar venda acima do saldo → erro esperado
4. Menu **1 (Carteira)** → **4 (Excluir)** carteira com movimentos → bloqueio

### Oráculo

1. Menu **3 (Oráculo)** → **1 (Cotação de hoje)**
2. **3 (Listar por período)** → ex.: `01/10/2026` a `05/10/2026` → **0 (Voltar)**

### Relatórios

1. Menu **4 (Relatórios)** → **1/2** listar carteiras por id/titular
2. **3 (Saldo atual)** e **4 (Histórico)** → informar ID da carteira
3. **5 (Ganhos e perdas)** → resultado por carteira a custo médio e a mercado (cotação de hoje)

### Oráculo simulado

Até a API do Oráculo existir, cada cotação é calculada pela fórmula da planilha de referência:

```
cotação = base + flutuação + valorização
valorização = 1,40 + 0,435 · ln(n)        (n = dias desde a data inicial; a data inicial é o dia 1)
flutuação   = sorteio em [-2,5; +2,5], sempre o mesmo para a mesma data
```

Não há cotação antes da data inicial (`01/01/2026`) nem para datas futuras. Os parâmetros ficam em
`src/main/resources/ftcoin.properties` e podem ser sobrescritos na execução, por exemplo
`java -Doraculo.base=20 -jar target/ft-coin-1.0.0-SNAPSHOT.jar`.

> No terminal do Windows, use `java -Dstdout.encoding=UTF-8 -jar ...` para os acentos aparecerem corretamente.

---

## Documentação


| Documento                                                | Descrição                                        |
| -------------------------------------------------------- | ------------------------------------------------ |
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md)         | Arquitetura, menus, CRUD e interação da carteira |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra/venda, Oráculo, saldo e diagramas         |
| [docs/fluxo-oraculo.md](docs/fluxo-oraculo.md)           | Consulta e listagem de cotações                  |
| [docs/fluxo-relatorios.md](docs/fluxo-relatorios.md)     | Listagens, saldo, histórico e ganho/perda        |
| [docs/README.md](docs/README.md)                         | Índice da pasta de documentação                  |


---

## Controle de versão

Trabalho em equipe com Git: cada funcionalidade ou camada em branch ou commits pequenos facilita revisão e o relatório de contribuição.