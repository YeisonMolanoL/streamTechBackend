# Script para validar sincronización de listeners IMAP en Windows
# Ejecutar como: powershell -ExecutionPolicy Bypass -File VALIDATE_SYNC.ps1

Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "🧪 VALIDACIÓN DE SINCRONIZACIÓN DE LISTENERS IMAP" -ForegroundColor Green
Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host ""

$BaseUrl = "http://localhost:8080/api/code-reception"

# Test 1: Health check
Write-Host "1. Verificando que backend está corriendo..." -ForegroundColor Blue
try {
    $health = Invoke-WebRequest "$BaseUrl/health" -ErrorAction Stop
    Write-Host "✓ Backend está corriendo" -ForegroundColor Green
} catch {
    Write-Host "✗ Backend NO está corriendo en localhost:8080" -ForegroundColor Red
    Write-Host "  Inicia el backend con: ./gradlew bootRun" -ForegroundColor Yellow
    exit 1
}
Write-Host ""

# Test 2: Get listener stats
Write-Host "2. Obteniendo estado de listeners..." -ForegroundColor Blue
try {
    $response = Invoke-WebRequest "$BaseUrl/listeners/stats" | ConvertFrom-Json
    
    Write-Host "Response:"
    Write-Host ($response | ConvertTo-Json -Depth 10)
    Write-Host ""
    
    if ($response.status -contains "SINCRONIZADO") {
        Write-Host "✓ Estado: SINCRONIZADO" -ForegroundColor Green
    } elseif ($response.status -contains "INCONSISTENTE") {
        Write-Host "⚠ Estado: INCONSISTENTE" -ForegroundColor Yellow
        Write-Host "  Inconsistencias detectadas: $($response.inconsistencies)" -ForegroundColor Yellow
    }
    
    Write-Host ""
    Write-Host "Estadísticas:" -ForegroundColor Blue
    Write-Host "  Listeners activos en memoria: $($response.memoryActiveNow)"
    Write-Host "  Cuentas activas en BD: $($response.databaseMarkedActive)"
    Write-Host "  Emails escuchando: $($response.activeEmails -join ', ')"
    
    if ($response.memoryActiveNow -eq $response.databaseMarkedActive) {
        Write-Host "✓ Perfecto: Memoria y BD sincronizados" -ForegroundColor Green
    } else {
        Write-Host "⚠ Advertencia: Hay discrepancia entre memoria y BD" -ForegroundColor Yellow
    }
    
} catch {
    Write-Host "✗ Error obteniendo estadísticas: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "════════════════════════════════════════════════════════════════" -ForegroundColor Cyan
Write-Host "✅ Validación completada" -ForegroundColor Green
Write-Host ""
Write-Host "📋 Endpoints disponibles para monitor:" -ForegroundColor Blue
Write-Host "   GET  $BaseUrl/health"
Write-Host "   GET  $BaseUrl/listeners/stats"
Write-Host "   GET  $BaseUrl/listeners/health/{email}"
Write-Host "   POST $BaseUrl/listeners/sync"
Write-Host ""
Write-Host "💡 Ejemplos de uso:" -ForegroundColor Yellow
Write-Host "   Invoke-WebRequest http://localhost:8080/api/code-reception/listeners/stats"
Write-Host "   Invoke-WebRequest http://localhost:8080/api/code-reception/listeners/health/usuario@example.com"
Write-Host "   Invoke-WebRequest -Method Post http://localhost:8080/api/code-reception/listeners/sync"
Write-Host ""
