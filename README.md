# FT_Coin

Sistema de apuração de ganhos e perdas em carteira de moedas virtuais, desenvolvido em **Java (8+)** com arquitetura **MVC**, interface **CLI** e persistência via padrão **DAO** (memória e MariaDB).

---

## Estrutura atual do projeto

```
FT_Coin/
├── out                                    # Saídas da aplicação
└── src
    ├── app
    │   └── Main.java                      # Ponto de entrada da aplicação
    ├── model/
    │   ├── Carteira.java                  # Entidade carteira
    │   ├── Movimentacao.java              # Entidade movimentação (compra/venda)
    │   └── Oraculo.java                   # Cotação diária da moeda virtual
    ├── DTO/
    │   ├── CarteiraDTO.java               # Transferência de dados — carteira
    │   └── MovimentacaoDTO.java           # Transferência de dados — movimentação
    ├── DAO/
    │   ├── CarteiraDAO.java               # Contrato de persistência — carteira
    │   ├── MovimentacaoDAO.java           # Contrato de persistência — movimentação
    │   ├── OraculoDAO.java                # Contrato de persistência — oráculo
    │   └── mariaDB/
    │       ├── CarteiraDAOMariaDB.java    # Implementação JDBC — carteira
    │       ├── MovimentacaoDAOMariaDB.java
    │       └── OraculoDAOMariaDB.java
    ├── controller/
    │   ├── CarteiraController.java        # Regras de negócio — carteira
    │   ├── MovimentacaoController.java    # Regras de negócio — movimentação
    │   └── RelatorioController.java       # Relatórios e apuração de ganho/perda
    ├── view/
    │   ├── ConsolePrinter.java            # Saída formatada e cores no terminal
    │   ├── MenuPrincipal.java             # Menu raiz (Carteira, Movimentação, …)
    │   ├── MenuCarteira.java
    │   ├── MenuMovimentacao.java
    │   ├── MenuRelatorios.java
    │   └── MenuAjuda.java
    ├── exception/
    │   └── AppException.java              # Exceções da aplicação
    └── infra/
        └── ConexaoBD.java                 # Conexão com MariaDB remoto
```

### Responsabilidade por camada

| Camada | Pacote / pasta | Papel |
|--------|----------------|-------|
| **Model** | `model/` | Representação das entidades de domínio alinhadas às tabelas do banco |
| **DTO** | `DTO/` | Objetos para transporte de dados entre camadas |
| **DAO** | `DAO/`, `DAO/mariaDB/` | Abstração e implementação de persistência (padrão DAO) |
| **Controller** | `controller/` | Validações, regras de negócio e orquestração |
| **View** | `view/` | Menus CLI, leitura de entrada e apresentação ao usuário |
| **Infra** | `infra/` | Recursos técnicos (conexão com banco) |
| **Exception** | `exception/` | Tratamento centralizado de erros |

### Modelo de dados (referência)

| Tabela | Campos principais |
|--------|---------------------|
| **Carteira** | `id` (PK), `nome_titular`, `corretora` |
| **Movimentação** | `id_carteira` (FK), `id_movimento` (PK), `data`, `tipo` (`C`/`V`), `quantidade` |
| **Oráculo** | `data` (PK), `cotacao` |

---

## Requisitos do sistema (escopo)

- **Interface:** CLI com menu principal (Carteira, Movimentação, Relatórios, Ajuda, Sair) e submenus; uso de cores quando apropriado.
- **Carteira:** incluir, consultar, editar e excluir.
- **Movimentação:** compra e venda de moeda virtual.
- **Relatórios:** listagens ordenadas, saldo, histórico e ganho/perda por carteira.
- **Oráculo:** consulta à cotação diária para cálculos e movimentações.
- **Persistência:** duas implementações DAO — **memória** (demonstração) e **MariaDB** (remoto).
- **Boas práticas:** POO, encapsulamento, polimorfismo, constantes/enums, validações, exceções, código sem erros ou avisos na compilação.

---

## Compilação e execução

> Atualize esta seção assim que `Main.java` e as dependências estiverem implementados.

**Pré-requisito:** JDK 8 ou superior.

```bash
# Na raiz do projeto (ajuste o classpath conforme novos pacotes forem adicionados)
javac -encoding UTF-8 -d out Main.java model/*.java DTO/*.java DAO/*.java DAO/mariaDB/*.java controller/*.java view/*.java exception/*.java infra/*.java

java -cp out Main
```

---

## Controle de versão

Trabalho em equipe com Git: cada funcionalidade ou camada em branch ou commits pequenos facilita revisão e o relatório de contribuição.

