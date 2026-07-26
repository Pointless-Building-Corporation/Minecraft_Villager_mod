param(
    [Parameter(Mandatory=$true)]
    [string]$Message,
    
    [Parameter(Mandatory=$false)]
    [string]$Type = "chore",
    
    [Parameter(Mandatory=$false)]
    [string]$Scope = "",
    
    [Parameter(Mandatory=$false)]
    [string[]]$Files = @(),
    
    [Parameter(Mandatory=$false)]
    [switch]$All = $false
)

$ErrorActionPreference = "Stop"

if ($Scope -ne "") {
    $commitMsg = "${Type}(${Scope}): ${Message}"
} else {
    $commitMsg = "${Type}: ${Message}"
}

Write-Host ">>> Preparing commit: '$commitMsg'" -ForegroundColor Cyan

if ($All -or $Files.Count -eq 0) {
    Write-Host "Staging all changes..." -ForegroundColor Yellow
    git add .
} else {
    foreach ($f in $Files) {
        Write-Host "Staging file: $f..." -ForegroundColor Yellow
        git add $f
    }
}

$status = git status --porcelain
if ($status -eq $null -or $status -eq "") {
    Write-Host "No changes staged or found to commit!" -ForegroundColor Green
    exit 0
}

Write-Host "Committing staged changes..." -ForegroundColor Yellow
git commit -m "$commitMsg"
if ($LASTEXITCODE -eq 0) {
    Write-Host ">>> Successfully committed!" -ForegroundColor Green
} else {
    Write-Host ">>> Commit failed!" -ForegroundColor Red
    exit $LASTEXITCODE
}
