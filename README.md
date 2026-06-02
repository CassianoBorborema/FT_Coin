# FT_Coin

Sistema de apuração de ganhos e perdas em carteira de moedas virtuais, desenvolvido em **Java (8+)** com arquitetura **MVC**, interface **CLI** e persistência via padrão **DAO** (memória e MariaDB).

---

## Estado atual do projeto

| Área | Status | Observação |
|------|--------|------------|
| **Menu principal** | Parcial | Opção **Carteira** integrada; Movimentação, Relatórios e Ajuda ainda sem navegação no `MenuPrincipal` |
| **Carteira (CRUD)** | Implementado | Incluir, consultar, editar e excluir com validações e confirmação de exclusão |
| **Persistência carteira** | Memória | `CarteiraDAOMemoria` (`HashMap` + IDs automáticos); MariaDB em stub |
| **Movimentação** | Pendente | Menus e enums criados; model, DTO, DAO e controller vazios |
| **Relatórios** | Pendente | Menu com opções esboçadas; `RelatorioController` vazio |
| **Oráculo** | Pendente | Model, DTO e DAO em stub |
| **Infra (MariaDB)** | Pendente | `ConexaoBD` e implementações JDBC em stub |

A aplicação **compila e executa** o fluxo de carteira ponta a ponta. Detalhes de arquitetura e sequência de chamadas: [docs/fluxo-carteira.md](docs/fluxo-carteira.md).

---

## Estrutura do projeto

```
FT_Coin/
├── docs/
│   ├── README.md                          # Índice da documentação
│   └── fluxo-carteira.md                  # Fluxo MVC + DAO da carteira
├── out/                                     # Classes compiladas (ignorado pelo Git)
└── src/
    ├── app/
    │   └── Main.java                      # Entrada; injeta DAO memória e MenuPrincipal
    ├── model/
    │   ├── Carteira.java                  # Entidade com validação e conversão DTO
    │   ├── Movimentacao.java              # (stub)
    │   └── Oraculo.java                   # (stub)
    ├── DTO/
    │   ├── CarteiraDTO.java
    │   └── MovimentacaoDTO.java           # (stub)
    ├── DAO/
    │   ├── CarteiraDAO.java               # Contrato — carteira
    │   ├── MovimentacaoDAO.java           # (stub)
    │   ├── OraculoDAO.java                # (stub)
    │   ├── memoria/
    │   │   └── CarteiraDAOMemoria.java    # Implementação em memória
    │   └── mariaDB/
    │       ├── CarteiraDAOMariaDB.java    # (stub)
    │       ├── MovimentacaoDAOMariaDB.java
    │       └── OraculoDAOMariaDB.java
    ├── controller/
    │   ├── CarteiraController.java
    │   ├── MovimentacaoController.java    # (stub)
    │   └── RelatorioController.java       # (stub)
    ├── view/
    │   ├── MenuPrincipal.java
    │   ├── MenuCarteira.java
    │   ├── MenuMovimentacao.java          # Estrutura; lógica pendente
    │   ├── MenuRelatorios.java            # Estrutura; lógica pendente
    │   ├── MenuAjuda.java                 # Créditos parcial; ajuda pendente
    │   └── opcoes_menus/
    │       ├── OpcoesMenuPrincipal.java
    │       ├── OpcoesMenuCarteira.java
    │       ├── OpcoesMenuMovimentacao.java
    │       ├── OpcoesMenuRelatorios.java
    │       └── OpcoesMenuAjuda.java
    ├── exception/
    │   └── AppException.java              # Exceção checked de negócio/validação
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
# Compilar (resolve dependências a partir de src/app/Main.java)
javac -encoding UTF-8 -sourcepath src -d out src/app/Main.java

# Executar
java -cp out app.Main
```

**PowerShell (Windows):**

```powershell
javac -encoding UTF-8 -sourcepath src -d out src/app/Main.java
java -cp out app.Main
```

> Arquivos `.java` vazios (stubs) não entram na compilação até serem implementados. Ao adicionar novas classes, mantenha `-sourcepath src` ou inclua explicitamente os novos arquivos no `javac`.

---

## Documentação

| Documento | Descrição |
|-----------|-----------|
| [docs/fluxo-carteira.md](docs/fluxo-carteira.md) | Arquitetura, menus, CRUD e interação entre componentes da carteira |
| [docs/README.md](docs/README.md) | Índice da pasta de documentação |

---

## Controle de versão

Trabalho em equipe com Git: cada funcionalidade ou camada em branch ou commits pequenos facilita revisão e o relatório de contribuição.
