param(
    [Parameter(Mandatory=$true)]
    [string]$EnvFile
)

if (!(Test-Path $EnvFile)) {
    Write-Error "Environment file '$EnvFile' not found"
    exit 1
}

Get-Content $EnvFile | Where-Object { $_ -match '=' } | ForEach-Object {
    $pair = $_ -split '=',2
    $name = $pair[0]
    $value = $pair[1]
    Set-Item -Path "Env:$name" -Value $value
}

$azCmd = Get-Command az -ErrorAction SilentlyContinue
if (-not $azCmd) {
    Write-Error "Azure CLI (az) not found. Please install it: https://learn.microsoft.com/en-us/cli/azure/install-azure-cli"
    exit 1
}

$null = az account show 2>$null
if ($LASTEXITCODE -ne 0) {
    az login --tenant $Env:TENANT_ID --subscription $Env:SUBSCRIPTION_ID | Out-Null
} else {
    az account set --subscription $Env:SUBSCRIPTION_ID | Out-Null
}

az acr login --name $Env:ACR_NAME
$RepoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
docker build -f "$PSScriptRoot/Dockerfile" -t $Env:IMAGE_NAME $RepoRoot
docker tag $Env:IMAGE_NAME "$($Env:ACR_NAME).azurecr.io/$($Env:IMAGE_NAME)"
docker push "$($Env:ACR_NAME).azurecr.io/$($Env:IMAGE_NAME)"

