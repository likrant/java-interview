$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
& "$scriptDir/deploy.ps1" "$scriptDir/prod.env"

