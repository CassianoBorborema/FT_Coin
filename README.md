# FT_Coin

Sistema de apuração de ganhos e perdas em carteira de moedas virtuais, desenvolvido em **Java (8+)** com arquitetura **MVC**, interface **CLI** e persistência via padrão **DAO** (em memória).

---

## Estado atual do projeto


| Área                     | Status       | Observação                                                                         |
| ------------------------ | ------------ | ---------------------------------------------------------------------------------- |
| **Menu principal**       | Implementado | Carteira (1), Movimentação (2), Oráculo (3), Relatórios (4) e Ajuda (5) integrados |
| **Carteira (CRUD)**      | Implementado | Incluir, consultar, editar e excluir com validações e confirmação de exclusão      |
| **Exclusão de carteira** | Implementado | Bloqueada quando existem movimentações vinculadas                                  |
| **Movimentação**         | Implementado | Compra e venda com validação de carteira, cotação (Oráculo) e saldo na venda       |
| **Oráculo**              | Implementado | Cadastrar, consultar e listar cotações; seed no `Main` (hoje e ontem)              |
| **Relatórios**           | Implementado | Listagens (id/titular), saldo, histórico e ganho/perda por carteira                |
| **Ajuda**                | Implementado | Texto "Como funciona" (guia de uso)                                                |
| **Persistência**         | Memória      | `CarteiraDAOMemoria`, `MovimentacaoDAOMemoria`, `OraculoDAOMemoria`                |


A aplicação **compila e executa** todos os fluxos ponta a ponta.


| Documento                                                | Conteúdo                                              |
| -------------------------------------------------------- | ----------------------------------------------------- |
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md)         | Arquitetura, menus, CRUD e interação da carteira      |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra/venda, Oráculo, saldo e diagramas de sequência |
| [docs/fluxo-oraculo.md](docs/fluxo-oraculo.md)           | Cadastro, consulta e listagem de cotações             |
| [docs/fluxo-relatorios.md](docs/fluxo-relatorios.md)     | Listagens, saldo, histórico e cálculo de ganho/perda  |


---

## Estrutura do projeto

```
FT_Coin/
├── docs/
│   ├── README.md
│   ├── fluxo-carteira.md
│   ├── fluxo-movimentacao.md
│   ├── fluxo-oraculo.md
│   └── fluxo-relatorios.md
├── out/                                     # Classes compiladas (ignorado pelo Git)
└── src/
    ├── app/
    │   └── Main.java                      # Bootstrap, seed oráculo, leitura CLI
    ├── model/
    │   ├── Carteira.java
    │   ├── Movimentacao.java
    │   ├── TipoMovimentacao.java
    │   └── Oraculo.java
    ├── DTO/
    │   ├── CarteiraDTO.java
    │   ├── MovimentacaoDTO.java
    │   └── OraculoDTO.java
    ├── DAO/
    │   ├── CarteiraDAO.java
    │   ├── MovimentacaoDAO.java
    │   ├── OraculoDAO.java
    │   └── memoria/
    │       ├── CarteiraDAOMemoria.java
    │       ├── MovimentacaoDAOMemoria.java
    │       └── OraculoDAOMemoria.java
    ├── controller/
    │   ├── CarteiraController.java
    │   ├── MovimentacaoController.java
    │   ├── OraculoController.java
    │   └── RelatorioController.java
    ├── view/
    │   ├── MenuPrincipal.java
    │   ├── MenuCarteira.java
    │   ├── MenuMovimentacao.java
    │   ├── MenuOraculo.java
    │   ├── MenuRelatorios.java
    │   ├── MenuAjuda.java
    │   └── opcoes_menus/
    └── exception/
        └── AppException.java
```

### Responsabilidade por camada


| Camada         | Pacote                      | Papel                                               |
| -------------- | --------------------------- | --------------------------------------------------- |
| **Model**      | `model`                     | Entidades de domínio e validações                   |
| **DTO**        | `DTO`                       | Transporte entre view, controller e DAO             |
| **DAO**        | `DAO`, `DAO.memoria`        | Contrato e implementação de persistência em memória |
| **Controller** | `controller`                | Regras de negócio e orquestração                    |
| **View**       | `view`, `view.opcoes_menus` | Menus CLI e enums de opções                         |
| **Exception**  | `exception`                 | Erros de aplicação centralizados                    |


### Modelo de dados (referência)


| Tabela           | Campos principais                                                               |
| ---------------- | ------------------------------------------------------------------------------- |
| **Carteira**     | `id` (PK), `nome_titular`, `corretora`                                          |
| **Movimentação** | `id_carteira` (FK), `id_movimento` (PK), `data`, `tipo` (`C`/`V`), `quantidade` |
| **Oráculo**      | `data` (PK), `cotacao`                                                          |


---

## Requisitos do sistema (escopo)

- **Interface:** CLI com menu principal (Carteira, Movimentação, Oráculo, Relatórios, Ajuda, Sair) e submenus.
- **Carteira:** incluir, consultar, editar e excluir.
- **Movimentação:** compra e venda de moeda virtual.
- **Oráculo:** cadastrar, consultar e listar cotações diárias para cálculos e movimentações.
- **Relatórios:** listagens ordenadas, saldo, histórico e ganho/perda por carteira.
- **Persistência:** implementação DAO **em memória**.
- **Boas práticas:** POO, encapsulamento, polimorfismo, validações, exceções, compilação sem erros.

---

## Compilação e execução

**Pré-requisito:** JDK 8 ou superior.

Na raiz do repositório:

```bash
javac -encoding UTF-8 -sourcepath src -d out src/app/Main.java
java -cp out app.Main
```

**PowerShell (Windows):**

```powershell
javac -encoding UTF-8 -sourcepath src -d out src/app/Main.java
java -cp out app.Main
```

> Com `-sourcepath src`, todas as classes referenciadas a partir de `Main` são compiladas automaticamente.

---

## Teste rápido

### Carteira

1. Menu **1 (Carteira)** → **1 (Incluir)** → titular e corretora → anotar ID
2. **2 (Consultar)** → informar ID → **0 (Voltar)**

### Movimentação

1. Menu **2 (Movimentação)** → **1 (Compra)** → ID da carteira, data de hoje (`dd/MM/yyyy`), quantidade (ex.: 10)
2. **2 (Venda)** → mesma carteira, data de hoje, quantidade ≤ saldo (ex.: 5)
3. Tentar venda acima do saldo → erro esperado
4. Menu **1 (Carteira)** → **4 (Excluir)** carteira com movimentos → bloqueio

### Oráculo

1. Menu **3 (Oráculo)** → **1 (Cadastrar)** → data e cotação
2. **3 (Listar)** → conferir cotações cadastradas → **0 (Voltar)**

### Relatórios

1. Menu **4 (Relatórios)** → **1/2** listar carteiras por id/titular
2. **3 (Saldo atual)** e **4 (Histórico)** → informar ID da carteira
3. **5 (Ganhos e perdas)** → resultado por carteira (requer cotação de hoje no Oráculo)

Cotações seed: **hoje** e **ontem** (ver `Main.seedOraculo()`).

---

## Documentação


| Documento                                                | Descrição                                        |
| -------------------------------------------------------- | ------------------------------------------------ |
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md)         | Arquitetura, menus, CRUD e interação da carteira |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra/venda, Oráculo, saldo e diagramas         |
| [docs/fluxo-oraculo.md](docs/fluxo-oraculo.md)           | Cadastro, consulta e listagem de cotações        |
| [docs/fluxo-relatorios.md](docs/fluxo-relatorios.md)     | Listagens, saldo, histórico e ganho/perda        |
| [docs/README.md](docs/README.md)                         | Índice da pasta de documentação                  |


---

## Controle de versão

Trabalho em equipe com Git: cada funcionalidade ou camada em branch ou commits pequenos facilita revisão e o relatório de contribuição.