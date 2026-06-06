# Fluxo de Movimentação (FT_Coin)

Este documento descreve **como o fluxo de movimentação funciona hoje** no código em `src/`, incluindo a dependência mínima do **Oráculo** (cotação por data).

---

## 1. Visão geral da arquitetura

O fluxo segue **MVC + DTO + DAO**, espelhando o padrão da carteira, com persistência em memória.

```mermaid
flowchart TB
    subgraph viewLayer [View]
        Main["app.Main"]
        MenuPrincipal["view.MenuPrincipal"]
        MenuMovimentacao["view.MenuMovimentacao"]
    end

    subgraph controllerLayer [Controller]
        MovimentacaoController["controller.MovimentacaoController"]
        CarteiraController["controller.CarteiraController"]
    end

    subgraph domainLayer [Domínio]
        MovimentacaoModel["model.Movimentacao"]
        MovimentacaoDTO["DTO.MovimentacaoDTO"]
        OraculoModel["model.Oraculo"]
        OraculoDTO["DTO.OraculoDTO"]
        TipoMov["model.TipoMovimentacao"]
        AppException["exception.AppException"]
    end

    subgraph persistenceLayer [Persistência]
        MovimentacaoDAO["DAO.MovimentacaoDAO"]
        MovimentacaoDAOMemoria["DAO.memoria.MovimentacaoDAOMemoria"]
        CarteiraDAO["DAO.CarteiraDAO"]
        OraculoDAO["DAO.OraculoDAO"]
        OraculoDAOMemoria["DAO.memoria.OraculoDAOMemoria"]
    end

    Main -->|"instancia DAOs e controllers"| MovimentacaoController
    MenuPrincipal -->|"opção 2"| MenuMovimentacao
    MenuMovimentacao -->|"compra/venda"| MovimentacaoController
    MovimentacaoController -->|"valida carteira"| CarteiraDAO
    MovimentacaoController -->|"valida cotação na data"| OraculoDAO
    MovimentacaoController -->|"valida via"| MovimentacaoModel
    MovimentacaoController -->|"saldo na venda"| MovimentacaoDAO
    MovimentacaoController -->|"persiste"| MovimentacaoDAO
    MovimentacaoDAO -.->|"implementa"| MovimentacaoDAOMemoria
    OraculoDAO -.->|"implementa"| OraculoDAOMemoria
    MenuMovimentacao -->|"captura e exibe"| AppException
```

### Papel de cada componente

| Componente | Arquivo | Responsabilidade no fluxo |
|------------|---------|---------------------------|
| **Main** | [src/app/Main.java](../src/app/Main.java) | Bootstrap: cria DAOs em memória, controllers, seed de cotações; helpers `lerData`, `lerDouble`, `lerInteiro` |
| **MenuPrincipal** | [src/view/MenuPrincipal.java](../src/view/MenuPrincipal.java) | Menu raiz; opção **2 (Movimentação)** abre `MenuMovimentacao` |
| **MenuMovimentacao** | [src/view/MenuMovimentacao.java](../src/view/MenuMovimentacao.java) | Submenu compra/venda; lê ID da carteira, data e quantidade |
| **MovimentacaoController** | [src/controller/MovimentacaoController.java](../src/controller/MovimentacaoController.java) | Valida carteira, cotação na data, saldo (venda); orquestra inclusão |
| **Movimentacao (model)** | [src/model/Movimentacao.java](../src/model/Movimentacao.java) | Valida campos; `fromDTO()` / `toDTO()` |
| **TipoMovimentacao** | [src/model/TipoMovimentacao.java](../src/model/TipoMovimentacao.java) | Enum `COMPRA (C)` e `VENDA (V)` |
| **MovimentacaoDTO** | [src/DTO/MovimentacaoDTO.java](../src/DTO/MovimentacaoDTO.java) | Transporte: `idMovimento`, `idCarteira`, `data`, `tipo`, `quantidade` |
| **Oraculo / OraculoDTO** | [src/model/Oraculo.java](../src/model/Oraculo.java), [src/DTO/OraculoDTO.java](../src/DTO/OraculoDTO.java) | Cotação diária usada para validar a data da movimentação |
| **MovimentacaoDAO** | [src/DAO/MovimentacaoDAO.java](../src/DAO/MovimentacaoDAO.java) | Contrato: incluir, consultar, listar por carteira, saldo, vínculos |
| **MovimentacaoDAOMemoria** | [src/DAO/memoria/MovimentacaoDAOMemoria.java](../src/DAO/memoria/MovimentacaoDAOMemoria.java) | `HashMap` + `proximoId` global |
| **OraculoDAOMemoria** | [src/DAO/memoria/OraculoDAOMemoria.java](../src/DAO/memoria/OraculoDAOMemoria.java) | `Map<LocalDate, OraculoDTO>` |

---

## 2. Bootstrap — como a aplicação inicia o fluxo

```mermaid
sequenceDiagram
    participant Main as app.Main
    participant CarteiraDAO as CarteiraDAOMemoria
    participant OraculoDAO as OraculoDAOMemoria
    participant MovDAO as MovimentacaoDAOMemoria
    participant CC as CarteiraController
    participant MC as MovimentacaoController
    participant MP as MenuPrincipal
    participant MM as MenuMovimentacao

    Main->>CarteiraDAO: new
    Main->>OraculoDAO: new + seedOraculo
    Main->>MovDAO: new
    Main->>CC: new CarteiraController carteiraDAO movDAO
    Main->>MC: new MovimentacaoController movDAO carteiraDAO oraculoDAO
    Main->>MP: new MenuPrincipal scanner CC MC
    MP->>MM: opcao 2 Movimentacao
    MM->>MM: exibirMenuMovimentacao
```

**Seed do Oráculo:** na inicialização, [Main.java](../src/app/Main.java) cadastra cotações para **hoje** e **ontem** (150,0 e 145,0), permitindo testar compra/venda sem menu de cotação.

**Injeção de dependências:** controllers recebem interfaces `*DAO`, não implementações concretas.

---

## 3. Navegação nos menus

```mermaid
stateDiagram-v2
    [*] --> MenuPrincipal
    MenuPrincipal --> MenuMovimentacao: opcao 2
    MenuMovimentacao --> Compra: opcao 1
    MenuMovimentacao --> Venda: opcao 2
    MenuMovimentacao --> MenuPrincipal: opcao 0 Voltar
    Compra --> MenuMovimentacao: conclui ou erro
    Venda --> MenuMovimentacao: conclui ou erro
    MenuPrincipal --> [*]: opcao 0 Sair
```

**Comportamento do loop:**

- Opções inválidas no submenu exibem mensagem e **não encerram** o programa (padrão `MenuCarteira`).
- O tipo (`C`/`V`) é definido pela opção do menu, não digitado pelo usuário.

---

## 4. Operações — passo a passo

### 4.1 Compra de moeda virtual

```mermaid
sequenceDiagram
    participant User as Usuário
    participant MM as MenuMovimentacao
    participant Main as app.Main
    participant Ctrl as MovimentacaoController
    participant CarteiraDAO as CarteiraDAO
    participant OraculoDAO as OraculoDAO
    participant Model as Movimentacao
    participant MovDAO as MovimentacaoDAOMemoria

    User->>MM: opcao 1 Compra
    MM->>Main: lerInteiro idCarteira
    MM->>Main: lerData data
    MM->>Main: lerDouble quantidade
    MM->>Ctrl: registrarCompra id data qtd
    Ctrl->>CarteiraDAO: existe idCarteira
    Ctrl->>OraculoDAO: consultarPorData data
    Ctrl->>Model: fromDTO tipo COMPRA + validar
    Ctrl->>MovDAO: incluir
    Note over MovDAO: idMovimento=0 gera proximoId
    Ctrl->>MovDAO: consultarPorId
    MovDAO-->>Ctrl: MovimentacaoDTO
    Ctrl-->>MM: MovimentacaoDTO
    MM->>User: exibe sucesso + toString
```

---

### 4.2 Venda de moeda virtual

```mermaid
sequenceDiagram
    participant User as Usuário
    participant MM as MenuMovimentacao
    participant Ctrl as MovimentacaoController
    participant MovDAO as MovimentacaoDAOMemoria

    User->>MM: opcao 2 Venda
    MM->>Ctrl: registrarVenda id data qtd
    Ctrl->>MovDAO: calcularSaldo idCarteira
    alt quantidade maior que saldo
        Ctrl-->>MM: AppException saldo insuficiente
        MM->>User: Erro mensagem
    else saldo suficiente
        Ctrl->>Ctrl: registrar tipo VENDA
        Ctrl-->>MM: MovimentacaoDTO
        MM->>User: exibe sucesso
    end
```

**Saldo:** `saldo = Σ compras − Σ vendas` por carteira, calculado em `MovimentacaoDAOMemoria.calcularSaldo()`.

---

## 5. Fluxo de dados (DTO vs Model)

```mermaid
flowchart LR
    subgraph view [View]
        Input["idCarteira, data, quantidade"]
    end

    subgraph controller [Controller]
        DTO1["MovimentacaoDTO"]
        Model["Movimentacao"]
        DTO2["MovimentacaoDTO"]
    end

    subgraph dao [DAO]
        Storage["HashMap de MovimentacaoDTO"]
    end

    Input --> controller
    DTO1 -->|"fromDTO()"| Model
    Model -->|"validar()"| Model
    Model -->|"toDTO()"| DTO2
    DTO2 -->|"incluir"| Storage
    Storage -->|"consultarPorId"| DTO2
    DTO2 --> view
```

---

## 6. Tratamento de erros

| Origem | Exemplo | Quem lança | Quem captura |
|--------|---------|------------|--------------|
| Entrada numérica inválida | "abc" como ID | `Main.lerInteiro()` | `MenuMovimentacao` |
| Data inválida | formato errado | `Main.lerData()` | `MenuMovimentacao` |
| Quantidade inválida | ≤ 0 | `Movimentacao.validar()` | `MenuMovimentacao` |
| Carteira inexistente | ID não cadastrado | `MovimentacaoController` | `MenuMovimentacao` |
| Sem cotação na data | data sem seed | `OraculoDAOMemoria` | `MenuMovimentacao` |
| Saldo insuficiente | venda acima do saldo | `MovimentacaoController` | `MenuMovimentacao` |
| Exclusão de carteira com movimentos | excluir após compra | `CarteiraController.excluir()` | `MenuCarteira` |

---

## 7. Estado persistido em memória

```mermaid
flowchart TB
    subgraph MovimentacaoDAOMemoria
        proximoId["proximoId: int"]
        map["movimentacoes: Map int MovimentacaoDTO"]
    end

    subgraph OraculoDAOMemoria
        cotacoes["cotacoes: Map LocalDate OraculoDTO"]
    end

    incluirOp["incluir()"] --> proximoId
    incluirOp --> map
    saldoOp["calcularSaldo()"] --> map
```

---

## 8. Mapa de arquivos envolvidos no fluxo

```
src/
├── app/Main.java                           ← bootstrap, seed oráculo, lerData/lerDouble
├── view/
│   ├── MenuPrincipal.java                  ← roteia opção 2
│   └── MenuMovimentacao.java               ← compra e venda
├── controller/
│   ├── MovimentacaoController.java
│   └── CarteiraController.java             ← bloqueio exclusão com movimentos
├── model/
│   ├── Movimentacao.java
│   ├── TipoMovimentacao.java
│   └── Oraculo.java
├── DTO/
│   ├── MovimentacaoDTO.java
│   └── OraculoDTO.java
├── DAO/
│   ├── MovimentacaoDAO.java
│   ├── OraculoDAO.java
│   └── memoria/
│       ├── MovimentacaoDAOMemoria.java
│       └── OraculoDAOMemoria.java
└── exception/AppException.java
```

---

## 9. Execução manual (referência)

```powershell
javac -encoding UTF-8 -sourcepath src -d out src/app/Main.java
java -cp out app.Main
```

**Caminho típico de teste:**

1. Menu Principal → **1 (Carteira)** → **1 (Incluir)** → anotar ID gerado → **0 (Voltar)**
2. Menu Principal → **2 (Movimentação)** → **1 (Compra)** → ID da carteira, data de hoje (`dd/MM/yyyy`), quantidade (ex.: 10)
3. **2 (Venda)** → mesma carteira, data de hoje, quantidade ≤ saldo (ex.: 5)
4. Tentar venda acima do saldo → mensagem de erro
5. Menu **1 (Carteira)** → **4 (Excluir)** carteira com movimentos → bloqueio
6. **0 (Voltar)** → **0 (Sair)**

**Datas com cotação seed:** hoje e ontem (definidas em `Main.seedOraculo()`).

---

## 10. Limitações atuais (contexto para evolução)

- `MovimentacaoDAO.listarPorCarteira()` e `calcularSaldo()` existem, mas **não são expostos** no menu — serão usados em Relatórios.
- Não há edição nem exclusão de movimentação individual no menu.
- Implementações JDBC (`MovimentacaoDAOMariaDB`, `OraculoDAOMariaDB`) ainda em stub.
- Cotações só são carregadas via seed no `Main`; não há menu para cadastrar cotação em runtime.
- Menu principal ainda não conecta Relatórios e Ajuda.
