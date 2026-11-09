# Carga las variables del .env de esta carpeta en la terminal actual, para correr servicios con
# spring-boot:run (Docker Compose lee el .env solo; Maven no).
# Uso: . C:\Proyectos\Proyecto-de-Titulo\promaty-backend\cargar-env.ps1   (con el punto y el espacio delante)

$envFile = Join-Path $PSScriptRoot ".env"

if (-not (Test-Path $envFile)) {
    Write-Host "No existe $envFile. Copia .env.example a .env y completa los valores." -ForegroundColor Red
    return
}

$loaded = 0
foreach ($line in Get-Content $envFile) {
    $trimmed = $line.Trim()
    if ($trimmed -eq "" -or $trimmed.StartsWith("#") -or -not $trimmed.Contains("=")) {
        continue
    }
    $name, $value = $trimmed -split "=", 2
    $value = $value.Trim().Trim('"').Trim("'")
    Set-Item -Path "env:$($name.Trim())" -Value $value
    $loaded++
}

Write-Host "Variables cargadas desde .env: $loaded" -ForegroundColor Green
