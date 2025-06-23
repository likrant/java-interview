$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
powershell.exe -ExecutionPolicy Bypass -File "$scriptDir/deploy.ps1" "$scriptDir/test.env"