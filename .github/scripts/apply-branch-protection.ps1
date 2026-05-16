# Aplica proteção da branch main via GitHub REST API.
# Pré-requisitos: permissão Admin no repo e um Personal Access Token (classic)
# com escopo "repo". Defina: $env:GITHUB_TOKEN = "ghp_..."
#
# Uso (na raiz do repositório):
#   pwsh .github/scripts/apply-branch-protection.ps1

$ErrorActionPreference = "Stop"

$owner = "CassianoBorborema"
$repo = "FT_Coin"
$branch = "main"

if (-not $env:GITHUB_TOKEN) {
    Write-Error "Defina GITHUB_TOKEN com um PAT que tenha escopo 'repo'."
}

$headers = @{
    Authorization = "Bearer $env:GITHUB_TOKEN"
    Accept        = "application/vnd.github+json"
    "X-GitHub-Api-Version" = "2022-11-28"
}

$body = @{
    required_status_checks = @{
        strict   = $true
        contexts = @("Compile Java")
    }
    enforce_admins                  = $true
    required_pull_request_reviews   = @{
        dismiss_stale_reviews           = $true
        require_code_owner_reviews      = $true
        required_approving_review_count = 1
    }
    restrictions = $null
    required_linear_history         = $false
    allow_force_pushes              = $false
    allow_deletions                 = $false
    block_creations                 = $false
    required_conversation_resolution = $false
} | ConvertTo-Json -Depth 5

$uri = "https://api.github.com/repos/$owner/$repo/branches/$branch/protection"

try {
    Invoke-RestMethod -Uri $uri -Method Put -Headers $headers -Body $body -ContentType "application/json"
    Write-Host "Proteção aplicada em '$branch'. Status check obrigatório: Compile Java"
    Write-Host "Se 'Compile Java' ainda não existir, rode um PR primeiro e execute este script novamente."
}
catch {
    Write-Error $_
}
