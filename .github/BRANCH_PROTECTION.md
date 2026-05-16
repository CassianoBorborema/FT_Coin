# Proteção da branch `main` (configuração manual no GitHub)

Quem tiver permissão de **Admin** no repositório deve aplicar estas opções em
**Settings → Branches → Add branch protection rule** (ou **Rules → Rulesets**).

Branch name pattern: `main`

| Opção | Valor |
|-------|-------|
| Require a pull request before merging | Sim |
| Required approvals | **1** |
| Dismiss stale pull request approvals when new commits are pushed | Sim |
| Require review from Code Owners | Sim |
| Require status checks to pass before merging | Sim |
| Status checks that are required | **Compile Java** |
| Require branches to be up to date before merging | Sim (recomendado) |
| Do not allow bypassing the above settings | Sim |
| Allow force pushes | Não |
| Allow deletions | Não |

> O status check **Compile Java** só aparece na lista depois que o workflow
> [ci.yml](workflows/ci.yml) rodar pelo menos uma vez em um PR.

## CodeRabbit (GitHub App)

1. Acesse https://app.coderabbit.ai/login
2. Conecte a conta GitHub e instale o app na organização/usuário.
3. Em **Settings → Repositories**, habilite apenas **FT_Coin**.
4. Abra um PR — a revisão automática usa [.coderabbit.yaml](../.coderabbit.yaml).

Comando manual em um PR: `@coderabbitai review`
