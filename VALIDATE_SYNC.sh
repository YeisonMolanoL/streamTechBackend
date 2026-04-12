#!/bin/bash
# Script para validar que la sincronización de listeners funciona correctamente

echo "════════════════════════════════════════════════════════════════"
echo "🧪 VALIDACIÓN DE SINCRONIZACIÓN DE LISTENERS IMAP"
echo "════════════════════════════════════════════════════════════════"
echo ""

BASE_URL="http://localhost:8080/api/code-reception"
HEADERS="Content-Type: application/json"

# Color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo -e "${BLUE}1. Verificando que backend está corriendo...${NC}"
if ! curl -s "$BASE_URL/health" > /dev/null; then
    echo -e "${RED}✗ Backend NO está corriendo en localhost:8080${NC}"
    echo "   Inicia el backend con: ./gradlew bootRun"
    exit 1
fi
echo -e "${GREEN}✓ Backend está corriendo${NC}"
echo ""

echo -e "${BLUE}2. Obteniendo estado de listeners...${NC}"
RESPONSE=$(curl -s "$BASE_URL/listeners/stats")
echo "Response: $RESPONSE"
echo ""

# Parsear respuesta
if echo "$RESPONSE" | grep -q "SINCRONIZADO"; then
    echo -e "${GREEN}✓ Estado: SINCRONIZADO${NC}"
elif echo "$RESPONSE" | grep -q "INCONSISTENTE"; then
    echo -e "${YELLOW}⚠ Estado: INCONSISTENTE${NC}"
    INCONSISTENT=$(echo "$RESPONSE" | grep -o '"inconsistencies":[0-9]*' | cut -d':' -f2)
    echo "  Hay $INCONSISTENT inconsistencias detectadas"
else
    echo -e "${YELLOW}≈ Estado desconocido${NC}"
fi
echo ""

# Extraer datos
ACTIVE=$(echo "$RESPONSE" | grep -o '"memoryActiveNow":[0-9]*' | cut -d':' -f2)
DATABASE=$(echo "$RESPONSE" | grep -o '"databaseMarkedActive":[0-9]*' | cut -d':' -f2)

if [ -n "$ACTIVE" ] && [ -n "$DATABASE" ]; then
    echo -e "${BLUE}Estadísticas:${NC}"
    echo "  Listeners activos en memoria: $ACTIVE"
    echo "  Cuentas activas en BD: $DATABASE"
    
    if [ "$ACTIVE" -eq "$DATABASE" ]; then
        echo -e "${GREEN}✓ Perfecto: Memoria y BD sincronizados${NC}"
    else
        echo -e "${YELLOW}⚠ Advertencia: Hay discrepancia entre memoria ($ACTIVE) y BD ($DATABASE)${NC}"
    fi
fi
echo ""

echo -e "${BLUE}3. Test de endpoints adicionales${NC}"
echo ""

# Test health endpoint
echo -e "   a) GET /health"
HEALTH=$(curl -s "$BASE_URL/health")
if echo "$HEALTH" | grep -q "UP"; then
    echo -e "      ${GREEN}✓ Health check OK${NC}"
else
    echo -e "      ${RED}✗ Health check falló${NC}"
fi

echo ""
echo "════════════════════════════════════════════════════════════════"
echo -e "${GREEN}✅ Validación completada${NC}"
echo ""
echo "📋 Endpoints disponibles para monitor:"
echo "   GET  $BASE_URL/health"
echo "   GET  $BASE_URL/listeners/stats"
echo "   GET  $BASE_URL/listeners/health/{email}"
echo "   POST $BASE_URL/listeners/sync"
echo ""
echo "💡 Para probar endpoints individuales:"
echo "   curl http://localhost:8080/api/code-reception/listeners/stats"
echo "   curl http://localhost:8080/api/code-reception/listeners/health/usuario@example.com"
echo "   curl -X POST http://localhost:8080/api/code-reception/listeners/sync"
echo ""
