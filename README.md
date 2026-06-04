# FT_Coin

Sistema de apuração de ganhos e perdas em carteira de moedas virtuais, desenvolvido em **Java (8+)** com arquitetura **MVC**, interface **CLI** e persistência via padrão **DAO** (memória e MariaDB).

---

## Estado atual do projeto

| Área | Status | Observação |
|------|--------|------------|
| **Menu principal** | Parcial | **Carteira** (opção 1) e **Movimentação** (opção 2) integradas; Relatórios e Ajuda ainda sem navegação |
| **Carteira (CRUD)** | Implementado | Incluir, consultar, editar e excluir com validações e confirmação de exclusão |
| **Exclusão de carteira** | Implementado | Bloqueada quando existem movimentações vinculadas |
| **Movimentação** | Implementado | Compra e venda com validação de carteira, cotação (Oráculo) e saldo na venda |
| **Oráculo** | Mínimo (memória) | Cotações seed no `Main` (hoje e ontem); sem menu de cadastro |
| **Persistência** | Memória | `CarteiraDAOMemoria`, `MovimentacaoDAOMemoria`, `OraculoDAOMemoria`; MariaDB em stub |
| **Relatórios** | Pendente | Menu com opções esboçadas; `RelatorioController` vazio |
| **Infra (MariaDB)** | Pendente | `ConexaoBD` e implementações JDBC em stub |

A aplicação **compila e executa** os fluxos de carteira e movimentação ponta a ponta.

| Documento | Conteúdo |
|-----------|----------|
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md) | Arquitetura, menus, CRUD e interação da carteira |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra/venda, Oráculo, saldo e diagramas de sequência |

---

## Estrutura do projeto

```
FT_Coin/
├── docs/
│   ├── README.md
│   ├── fluxo-carteira.md
│   └── fluxo-movimentacao.md
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
    │   ├── memoria/
    │   │   ├── CarteiraDAOMemoria.java
    │   │   ├── MovimentacaoDAOMemoria.java
    │   │   └── OraculoDAOMemoria.java
    │   └── mariaDB/                       # (stubs JDBC)
    ├── controller/
    │   ├── CarteiraController.java
    │   ├── MovimentacaoController.java
    │   └── RelatorioController.java       # (stub)
    ├── view/
    │   ├── MenuPrincipal.java
    │   ├── MenuCarteira.java
    │   ├── MenuMovimentacao.java
    │   ├── MenuRelatorios.java            # (estrutura)
    │   ├── MenuAjuda.java
    │   └── opcoes_menus/
    ├── exception/
    │   └── AppException.java
    └── infra/
        └── ConexaoBD.java                 # (stub)
```

### Responsabilidade por camada

| Camada | Pacote | Papel |
|--------|--------|-------|
| **Model** | `model` | Entidades de domínio e validações |
| **DTO** | `DTO` | Transporte entre view, controller e DAO |
| **DAO** | `DAO`, `DAO.memoria`, `DAO.mariaDB` | Contrato e implementações de persistência |
| **Controller** | `controller` | Regras de negócio e orquestração |
| **View** | `view`, `view.opcoes_menus` | Menus CLI e enums de opções |
| **Infra** | `infra` | Conexão com banco (futuro) |
| **Exception** | `exception` | Erros de aplicação centralizados |

### Modelo de dados (referência)

| Tabela | Campos principais |
|--------|---------------------|
| **Carteira** | `id` (PK), `nome_titular`, `corretora` |
| **Movimentação** | `id_carteira` (FK), `id_movimento` (PK), `data`, `tipo` (`C`/`V`), `quantidade` |
| **Oráculo** | `data` (PK), `cotacao` |

---

## Requisitos do sistema (escopo)

- **Interface:** CLI com menu principal (Carteira, Movimentação, Relatórios, Ajuda, Sair) e submenus.
- **Carteira:** incluir, consultar, editar e excluir.
- **Movimentação:** compra e venda de moeda virtual.
- **Relatórios:** listagens ordenadas, saldo, histórico e ganho/perda por carteira.
- **Oráculo:** consulta à cotação diária para cálculos e movimentações.
- **Persistência:** duas implementações DAO — **memória** (demonstração) e **MariaDB** (remoto).
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

> Com `-sourcepath src`, novas classes referenciadas são compiladas automaticamente. Stubs vazios (JDBC, `RelatorioController`) não entram na compilação até serem implementados.

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

Cotações seed: **hoje** e **ontem** (ver `Main.seedOraculo()`).

---

## Documentação

| Documento | Descrição |
|-----------|-----------|
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md) | Arquitetura, menus, CRUD e interação da carteira |
| [docs/fluxo-movimentacao.md](docs/fluxo-movimentacao.md) | Compra/venda, Oráculo, saldo e diagramas |
| [docs/README.md](docs/README.md) | Índice da pasta de documentação |

---

## Controle de versão

Trabalho em equipe com Git: cada funcionalidade ou camada em branch ou commits pequenos facilita revisão e o relatório de contribuição.
