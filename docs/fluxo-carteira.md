# Fluxo de Carteira (FT_Coin)

Este documento descreve **como o fluxo de carteira funciona hoje** no código em `src/`.

---

## 1. Visão geral da arquitetura

O fluxo segue **MVC + DTO + DAO**, com persistência em memória para demonstração.

```mermaid
flowchart TB
    subgraph viewLayer [View]
        Main["app.Main"]
        MenuPrincipal["view.MenuPrincipal"]
        MenuCarteira["view.MenuCarteira"]
    end

    subgraph controllerLayer [Controller]
        CarteiraController["controller.CarteiraController"]
    end

    subgraph domainLayer [Domínio]
        CarteiraModel["model.Carteira"]
        CarteiraDTO["DTO.CarteiraDTO"]
        AppException["exception.AppException"]
    end

    subgraph persistenceLayer [Persistência]
        CarteiraDAO["DAO.CarteiraDAO"]
        CarteiraDAOMemoria["DAO.memoria.CarteiraDAOMemoria"]
    end

    Main -->|"instancia"| CarteiraDAOMemoria
    Main -->|"instancia"| CarteiraController
    Main -->|"instancia"| MenuPrincipal
    MenuPrincipal -->|"opção 1"| MenuCarteira
    MenuCarteira -->|"CRUD"| CarteiraController
    CarteiraController -->|"valida via"| CarteiraModel
    CarteiraController -->|"converte DTO ↔ Model"| CarteiraDTO
    CarteiraController -->|"delega persistência"| CarteiraDAO
    CarteiraDAO -.->|"implementa"| CarteiraDAOMemoria
    CarteiraModel -->|"lança"| AppException
    CarteiraController -->|"propaga"| AppException
    MenuCarteira -->|"captura e exibe"| AppException
```

### Papel de cada componente

| Componente | Arquivo | Responsabilidade no fluxo |
|------------|---------|---------------------------|
| **Main** | [src/app/Main.java](../src/app/Main.java) | Bootstrap: cria DAOs em memória, `CarteiraController`, `MovimentacaoController` e `MenuPrincipal`; helpers `lerOpcao`, `lerLinha`, `lerInteiro`, `lerData`, `lerDouble` |
| **MenuPrincipal** | [src/view/MenuPrincipal.java](../src/view/MenuPrincipal.java) | Menu raiz; opção **1 (Carteira)** abre `MenuCarteira`; opção **2 (Movimentação)** abre `MenuMovimentacao` (ver [fluxo-movimentacao.md](fluxo-movimentacao.md)) |
| **MenuCarteira** | [src/view/MenuCarteira.java](../src/view/MenuCarteira.java) | Submenu CRUD; lê dados do usuário, chama o controller e exibe resultado ou erro |
| **CarteiraController** | [src/controller/CarteiraController.java](../src/controller/CarteiraController.java) | Orquestra regras: valida identificador, monta DTO, converte para `Carteira`, valida campos e delega ao DAO |
| **Carteira (model)** | [src/model/Carteira.java](../src/model/Carteira.java) | Entidade de domínio; valida `nomeTitular` e `corretora`; converte `fromDTO()` / `toDTO()` |
| **CarteiraDTO** | [src/DTO/CarteiraDTO.java](../src/DTO/CarteiraDTO.java) | Objeto de transferência entre view, controller e DAO (3 campos: `identificador`, `nomeTitular`, `corretora`) |
| **CarteiraDAO** | [src/DAO/CarteiraDAO.java](../src/DAO/CarteiraDAO.java) | Contrato de persistência (interface) |
| **CarteiraDAOMemoria** | [src/DAO/memoria/CarteiraDAOMemoria.java](../src/DAO/memoria/CarteiraDAOMemoria.java) | Implementação com `HashMap<Integer, CarteiraDTO>`; gera IDs automaticamente |
| **AppException** | [src/exception/AppException.java](../src/exception/AppException.java) | Exceção checked para erros de negócio e validação |

---

## 2. Bootstrap — como a aplicação inicia o fluxo

```mermaid
sequenceDiagram
    participant User as Usuário
    participant Main as app.Main
    participant DAO as CarteiraDAOMemoria
    participant Ctrl as CarteiraController
    participant MP as MenuPrincipal
    participant MC as MenuCarteira

    User->>Main: java -cp out app.Main
    Main->>DAO: new CarteiraDAOMemoria()
    Main->>Ctrl: new CarteiraController(carteiraDAO, movimentacaoDAO)
    Main->>MP: new MenuPrincipal(scanner, ctrl, movimentacaoCtrl)
    Main->>User: exibe boas-vindas
    Main->>MP: exibirMenuPrincipal()
    User->>MP: digita 1 (Carteira)
    MP->>MC: new MenuCarteira(scanner, ctrl)
    MP->>MC: exibirMenuCarteira()
```

**Injeção de dependências:** o `CarteiraController` recebe a interface `CarteiraDAO`, não a implementação concreta. Hoje [Main.java](../src/app/Main.java) usa `CarteiraDAOMemoria`; no futuro pode trocar por `CarteiraDAOMariaDB` sem alterar controller ou view.

---

## 3. Navegação nos menus

```mermaid
stateDiagram-v2
    [*] --> MenuPrincipal
    MenuPrincipal --> MenuCarteira: opcao 1
    MenuCarteira --> Incluir: opcao 1
    MenuCarteira --> Consultar: opcao 2
    MenuCarteira --> Editar: opcao 3
    MenuCarteira --> Excluir: opcao 4
    MenuCarteira --> MenuPrincipal: opcao 0 Voltar
    Incluir --> MenuCarteira: conclui ou erro
    Consultar --> MenuCarteira: conclui ou erro
    Editar --> MenuCarteira: conclui ou erro
    Excluir --> MenuCarteira: conclui ou erro
    MenuPrincipal --> [*]: opcao 0 Sair
```

**Comportamento do loop:**

- `MenuPrincipal` e `MenuCarteira` usam `while (true)` — após cada operação, o submenu permanece aberto até o usuário escolher **0 (Voltar/Sair)**.
- Opções inválidas no submenu de carteira exibem mensagem e **não encerram** o programa (diferente do menu principal, que lança `RuntimeException`).

---

## 4. Operações CRUD — passo a passo

### 4.1 Incluir carteira

```mermaid
sequenceDiagram
    participant User as Usuário
    participant MC as MenuCarteira
    participant Main as app.Main
    participant Ctrl as CarteiraController
    participant Model as model.Carteira
    participant DAO as CarteiraDAOMemoria

    User->>MC: opcao 1
    MC->>Main: lerLinha(nomeTitular)
    MC->>Main: lerLinha(corretora)
    MC->>Ctrl: incluir(nomeTitular, corretora)
    Ctrl->>Ctrl: new CarteiraDTO(0, nome, corretora)
    Ctrl->>Model: fromDTO(dto)
    Ctrl->>Model: validar()
    Note over Model: rejeita titular/corretora vazios
    Ctrl->>Model: toDTO()
    Ctrl->>DAO: incluir(dtoParaSalvar)
    Note over DAO: id=0 gera proximoId automaticamente
    DAO->>DAO: grava copia no HashMap
    Ctrl->>DAO: consultarPorId(idGerado)
    DAO-->>Ctrl: CarteiraDTO
    Ctrl-->>MC: CarteiraDTO
    MC->>User: exibe sucesso + toString()
```

**Regras aplicadas:**

- ID inicia em `0`; o DAO atribui `proximoId` (1, 2, 3…).
- Validação de campos ocorre no **model**, não na view.
- O DAO armazena uma **cópia** do DTO para evitar mutação externa.

---

### 4.2 Consultar carteira

```mermaid
sequenceDiagram
    participant User as Usuário
    participant MC as MenuCarteira
    participant Main as app.Main
    participant Ctrl as CarteiraController
    participant DAO as CarteiraDAOMemoria

    User->>MC: opcao 2
    MC->>Main: lerInteiro(identificador)
    MC->>Ctrl: consultar(id)
    Ctrl->>Ctrl: validarIdentificador(id > 0)
    Ctrl->>DAO: consultarPorId(id)
    alt carteira existe
        DAO-->>Ctrl: copia do CarteiraDTO
        Ctrl-->>MC: CarteiraDTO
        MC->>User: exibe dados
    else nao encontrada
        DAO-->>Ctrl: AppException
        Ctrl-->>MC: AppException
        MC->>User: Erro: mensagem
    end
```

---

### 4.3 Editar carteira

```mermaid
sequenceDiagram
    participant User as Usuário
    participant MC as MenuCarteira
    participant Ctrl as CarteiraController
    participant Model as model.Carteira
    participant DAO as CarteiraDAOMemoria

    User->>MC: opcao 3
    MC->>Ctrl: consultar(id)
    Ctrl->>DAO: consultarPorId(id)
    DAO-->>MC: CarteiraDTO atual
    MC->>User: exibe carteira atual
    MC->>User: pede novo titular e corretora
    MC->>Ctrl: editar(id, nome, corretora)
    Ctrl->>Ctrl: validarIdentificador + montar DTO
    Ctrl->>Model: fromDTO + validar()
    Ctrl->>DAO: atualizar(dto)
    Ctrl->>DAO: consultarPorId(id)
    DAO-->>MC: CarteiraDTO atualizada
    MC->>User: exibe sucesso
```

**Observação:** a edição exige que a carteira exista (consulta prévia) e revalida titular/corretora antes de persistir.

---

### 4.4 Excluir carteira

```mermaid
sequenceDiagram
    participant User as Usuário
    participant MC as MenuCarteira
    participant Ctrl as CarteiraController
    participant DAO as CarteiraDAOMemoria

    User->>MC: opcao 4
    MC->>Ctrl: consultar(id)
    Ctrl->>DAO: consultarPorId(id)
    DAO-->>MC: CarteiraDTO
    MC->>User: exibe carteira + pede confirmacao S/N
    alt confirmacao S
        MC->>Ctrl: excluir(id)
        alt possui movimentacoes
            Ctrl-->>MC: AppException
            MC->>User: Erro mensagem
        else sem movimentacoes
            Ctrl->>DAO: excluir(id)
            DAO->>DAO: remove do HashMap
            MC->>User: Carteira excluida
        end
    else confirmacao N ou outro
        MC->>User: Exclusao cancelada
    end
```

**Observação:** antes de excluir, `CarteiraController` consulta `MovimentacaoDAO.possuiMovimentacoes(id)`. Carteiras com compras ou vendas **não podem** ser removidas.

---

## 5. Fluxo de dados (DTO vs Model)

```mermaid
flowchart LR
    subgraph view [View]
        Input["Entrada do usuário<br/>String / int"]
    end

    subgraph controller [Controller]
        DTO1["CarteiraDTO"]
        Model["Carteira"]
        DTO2["CarteiraDTO"]
    end

    subgraph dao [DAO]
        Storage["HashMap de CarteiraDTO"]
    end

    Input -->|"primitivos"| controller
    DTO1 -->|"fromDTO()"| Model
    Model -->|"validar()"| Model
    Model -->|"toDTO()"| DTO2
    DTO2 -->|"incluir/atualizar"| Storage
    Storage -->|"consultarPorId/listar"| DTO2
    DTO2 -->|"retorno"| view
```

**Por que duas representações?**

- **CarteiraDTO:** transporta dados entre camadas sem expor lógica de domínio à view/DAO.
- **Carteira (model):** concentra validações de negócio (`validar()`).
- O controller é o **único ponto** que converte entre DTO e Model.

---

## 6. Tratamento de erros

| Origem | Exemplo | Quem lança | Quem captura |
|--------|---------|------------|--------------|
| Entrada numérica inválida | usuário digita "abc" como ID | `Main.lerInteiro()` → `AppException` | `MenuCarteira` (catch) |
| Titular/corretora vazios | campos em branco | `Carteira.validar()` → `AppException` | `MenuCarteira` (catch) |
| ID inválido | id <= 0 | `CarteiraController.validarIdentificador()` | `MenuCarteira` (catch) |
| Carteira inexistente | id 99 não cadastrado | `CarteiraDAOMemoria.consultarPorId()` | `MenuCarteira` (catch) |
| Carteira com movimentações | excluir após compra/venda | `CarteiraController.excluir()` | `MenuCarteira` (catch) |
| Opção inválida no menu principal | digita 9 | `RuntimeException` | `Main.main()` (catch genérico) |

Todas as operações do submenu exibem `Erro: {mensagem}` e **retornam ao loop do menu**, sem encerrar a aplicação.

---

## 7. Estado persistido em memória

```mermaid
flowchart TB
    subgraph CarteiraDAOMemoria
        proximoId["proximoId: int<br/>inicia em 1"]
        map["carteiras: Map int CarteiraDTO"]
    end

    incluirOp["incluir()"] -->|"id=0"| proximoId
    incluirOp -->|"grava copia"| map
    consultarOp["consultarPorId()"] -->|"retorna copia"| map
    atualizarOp["atualizar()"] -->|"substitui entrada"| map
    excluirOp["excluir()"] -->|"remove chave"| map
```

**Importante:** os dados existem apenas enquanto a JVM está ativa. Ao sair (`System.exit(0)`), todo o conteúdo do `HashMap` é perdido.

---

## 8. Mapa de arquivos envolvidos no fluxo

```
src/
├── app/Main.java                      ← bootstrap + leitura CLI
├── view/
│   ├── MenuPrincipal.java             ← roteia para MenuCarteira
│   └── MenuCarteira.java              ← 4 operações CRUD
├── controller/CarteiraController.java ← orquestração (+ bloqueio exclusão)
├── model/Carteira.java                ← validação + conversão
├── DTO/CarteiraDTO.java               ← transporte de dados
├── DAO/
│   ├── CarteiraDAO.java               ← contrato
│   └── memoria/CarteiraDAOMemoria.java← persistência atual
└── exception/AppException.java        ← erros de negócio
```

---

## 9. Execução manual (referência)

```powershell
javac -encoding UTF-8 -sourcepath src -d out src/app/Main.java
java -cp out app.Main
```

**Caminho típico de teste:** Menu Principal → 1 (Carteira) → 1 (Incluir) → informar titular e corretora → 2 (Consultar) → informar ID gerado → 0 (Voltar) → 0 (Sair).

---

## 10. Limitações atuais (contexto para evolução)

- `CarteiraController.listarTodas()` existe, mas **não é exposta** no menu — será usada futuramente em Relatórios.
- Implementação JDBC (`CarteiraDAOMariaDB`) ainda vazia; produção usaria a mesma interface `CarteiraDAO`.
- Exclusão bloqueia carteiras com movimentações vinculadas (ver [fluxo-movimentacao.md](fluxo-movimentacao.md)).
- Menu principal ainda não conecta Relatórios e Ajuda; Movimentação está integrada (opção 2).
