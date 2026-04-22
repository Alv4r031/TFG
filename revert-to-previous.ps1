# Script para revertir el repo local al commit anterior (crea backup)
# Uso: Abrir PowerShell en la raíz del repo y ejecutar: .\revert-to-previous.ps1

param(
    [string]$TargetSHA = 'c14ce1a577132354b63fd4bcb5bf175da57c73b0',
    [switch]$ForcePush
)

function AbortIfError($code, $msg) {
    if ($code -ne 0) {
        Write-Error $msg
        exit $code
    }
}

Write-Host "== Revert to previous commit helper =="

# Comprobar que git está disponible
if (-not (Get-Command git -ErrorAction SilentlyContinue)) {
    Write-Error "git no se encuentra en PATH. Instala git o abre PowerShell donde esté disponible."
    exit 1
}

# Asegurar que estamos en la raíz del repo (buscar .git)
if (-not (Test-Path .git)) {
    Write-Error "No se encontró una carpeta .git en el directorio actual. Ejecuta este script desde la raíz del repo: C:\Users\jeage\AndroidStudioProjects\LudoMix"
    exit 1
}

# Mostrar estado actual
Write-Host "Estado actual de git:";
git status --porcelain

# Crear rama de backup
$ts = Get-Date -Format "yyyyMMdd-HHmmss"
$backup = "backup-before-revert-$ts"
Write-Host "Creando rama de backup: $backup"
git branch $backup
AbortIfError $LASTEXITCODE "Fallo al crear la rama de backup"

# Hacer reset hard al SHA objetivo
Write-Host "Aplicando: git reset --hard $TargetSHA"
git reset --hard $TargetSHA
AbortIfError $LASTEXITCODE "Fallo al hacer git reset --hard $TargetSHA"

Write-Host "Reset aplicado. HEAD ahora apunta a:";
git --no-pager log --oneline -n 5

if ($ForcePush) {
    Write-Host "--force push al remoto origin/main (ADVERTENCIA: sobrescribirá la rama remota)"
    git push --force origin main
    AbortIfError $LASTEXITCODE "Fallo al hacer git push --force"
    Write-Host "Push forzado completado."
} else {
    Write-Host "No se realizó push forzado. Si quieres que GitHub refleje este estado, ejecuta: git push --force origin main"
}

Write-Host "Hecho. Ahora reconstruye el proyecto con: .\\gradlew.bat clean && .\\gradlew.bat assembleDebug"
