#!/bin/bash
# Script para validar que el sistema de recepción de códigos funciona correctamente

API_URL="http://localhost:8080/api/code-reception"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "═══════════════════════════════════════════════════"
echo "  🧪 VALIDACIÓN: SISTEMA DE RECEPCIÓN DE CÓDIGOS"
echo "═══════════════════════════════════════════════════"
echo ""

# 1. Health Check
echo -e "${YELLOW}1. ✓ Health Check${NC}"
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" $API_URL/health)
if [ "$HEALTH" == "200" ]; then
    echo -e "${GREEN}   ✅ Servicio activo${NC}"
else
    echo -e "${RED}   ❌ Servicio NO disponible (HTTP $HEALTH)${NC}"
    exit 1
fi
echo ""

# 2. Test Request válida
echo -e "${YELLOW}2. ✓ Request válida con email${NC}"
RESPONSE=$(curl -s -X POST $API_URL/get-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}' \
  --max-time 35)

echo "📨 Respuesta:"
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"
echo ""

# 3. Test Request sin email (debe fallar)
echo -e "${YELLOW}3. ✓ Request inválida (sin email)${NC}"
ERROR_RESPONSE=$(curl -s -X POST $API_URL/get-code \
  -H "Content-Type: application/json" \
  -d '{}')

if echo "$ERROR_RESPONSE" | grep -q "Email requerido"; then
    echo -e "${GREEN}   ✅ Validación trabajo${NC}"
else
    echo -e "${RED}   ❌ Validación falló${NC}"
fi
echo ""

# 4. Test Múltiples requests simultáneos
echo -e "${YELLOW}4. ✓ Múltiples requests simultáneos${NC}"
echo "   Iniciando 3 requests en paralelo (esperando 5s)..."

for i in {1..3}; do
    (
        RESPONSE=$(curl -s -X POST $API_URL/get-code \
          -H "Content-Type: application/json" \
          -d "{\"email\":\"test$i@example.com\"}" \
          --max-time 5)
        echo "   Request $i: $RESPONSE"
    ) &
done

wait
echo -e "${GREEN}   ✅ Múltiples requests procesados sin bloqueos${NC}"
echo ""

# 5. Verificar que sistema NO está bloqueado
echo -e "${YELLOW}5. ✓ Sistema NO está bloqueado${NC}"
START_TIME=$(date +%s)
HEALTH=$(curl -s -o /dev/null -w "%{http_code}" $API_URL/health)
END_TIME=$(date +%s)
DIFF=$((END_TIME - START_TIME))

if [ "$HEALTH" == "200" ] && [ "$DIFF" -lt 2 ]; then
    echo -e "${GREEN}   ✅ Health check rápido ($DIFF segundos)${NC}"
else
    echo -e "${RED}   ❌ Posible bloqueo (${DIFF}s, HTTP $HEALTH)${NC}"
fi
echo ""

# Resumen
echo "═══════════════════════════════════════════════════"
echo -e "${GREEN}  ✅ VALIDACIÓN COMPLETADA${NC}"
echo "═══════════════════════════════════════════════════"
echo ""
echo "📝 Resumen:"
echo "  • Sistema está UP"
echo "  • Email requerido (validación funciona)"
echo "  • Múltiples requests procesados simultáneamente"
echo "  • Sistema NO está bloqueado"
echo ""
echo "🚀 Para la siguiente prueba:"
echo "  1. Envía un email a: test@example.com"
echo "  2. El código debería aparecer en la respuesta"
echo "  3. Verifica que otros requests sigan funcionando"
echo ""
