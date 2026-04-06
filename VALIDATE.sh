#!/bin/bash

# ============================================================
# VALIDACIÓN FINAL - SISTEMA RECEPCIÓN DE CÓDIGOS
# ============================================================
# Este script verifica que todos los archivos fueron creados

echo "============================================================"
echo "  🔍 VALIDACIÓN - SISTEMA RECEPCIÓN DE CÓDIGOS"
echo "============================================================"
echo ""

# Colores
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

passed=0
failed=0

check_file() {
    local file=$1
    local description=$2
    
    if [ -f "$file" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ((passed++))
    else
        echo -e "${RED}✗${NC} $description (NOT FOUND: $file)"
        ((failed++))
    fi
}

check_dir() {
    local dir=$1
    local description=$2
    
    if [ -d "$dir" ]; then
        echo -e "${GREEN}✓${NC} $description"
        ((passed++))
    else
        echo -e "${RED}✗${NC} $description (NOT FOUND: $dir)"
        ((failed++))
    fi
}

# ============================================================
# BACKEND
# ============================================================
echo ""
echo "📦 BACKEND (Spring Boot)"
echo "---"

# Entidades
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/entities/EmailAccount.java" "Entity: EmailAccount"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/entities/Code.java" "Entity: Code"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/entities/CodeRequest.java" "Entity: CodeRequest"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/entities/ParsingRule.java" "Entity: ParsingRule"

# Repositorios
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/repositories/EmailAccountRepository.java" "Repository: EmailAccountRepository"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/repositories/CodeRepository.java" "Repository: CodeRepository"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/repositories/CodeRequestRepository.java" "Repository: CodeRequestRepository"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/repositories/ParsingRuleRepository.java" "Repository: ParsingRuleRepository"

# Servicios
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/services/EncryptionService.java" "Service: EncryptionService"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/services/ImapManagerService.java" "Service: ImapManagerService"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/services/CodeAssignmentService.java" "Service: CodeAssignmentService"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/services/LongPollingService.java" "Service: LongPollingService"

# DTOs
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/dtos/GetCodeRequestDto.java" "DTO: GetCodeRequestDto"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/dtos/CodeResponseDto.java" "DTO: CodeResponseDto"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/dtos/AddEmailAccountRequestDto.java" "DTO: AddEmailAccountRequestDto"

# Controlador
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/controllers/CodeReceptionController.java" "Controller: CodeReceptionController"

# Configuración
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/config/AsyncConfig.java" "Config: AsyncConfig"
check_file "streamTech-back/src/main/java/com/TechPulseInnovations/streamTech/codeReception/config/WebConfig.java" "Config: WebConfig"

# Resources
check_file "streamTech-back/src/main/resources/sql/code-reception-schema.sql" "SQL Script: code-reception-schema.sql"

# ============================================================
# FRONTEND
# ============================================================
echo ""
echo "🎨 FRONTEND (Angular)"
echo "---"

check_file "streamTech/src/app/modules/code-reception/components/code-reception.component.ts" "Component: TypeScript"
check_file "streamTech/src/app/modules/code-reception/components/code-reception.component.html" "Component: Template"
check_file "streamTech/src/app/modules/code-reception/components/code-reception.component.css" "Component: Styles"
check_file "streamTech/src/app/modules/code-reception/services/code-reception.service.ts" "Service: CodeReceptionService"
check_file "streamTech/src/app/modules/code-reception/code-reception.module.ts" "Module: CodeReceptionModule"
check_file "streamTech/src/app/modules/code-reception/code-reception-routing.module.ts" "Module: Routing"

# ============================================================
# DOCUMENTACIÓN
# ============================================================
echo ""
echo "📚 DOCUMENTACIÓN"
echo "---"

check_file "GUIA_CODE_RECEPTION.md" "Documentation: GUIA_CODE_RECEPTION (80KB)"
check_file "QUICK_START.md" "Documentation: QUICK_START"
check_file "README_CODE_RECEPTION.md" "Documentation: README"
check_file "FAQ.md" "Documentation: FAQ (20 preguntas)"
check_file "FLUJO_DIAGRAMAS.md" "Documentation: Diagramas de Flujo"
check_file "MANIFEST.js" "Documentation: Manifest"

# ============================================================
# CONFIGURACIÓN
# ============================================================
echo ""
echo "⚙️  CONFIGURACIÓN"
echo "---"

check_file ".env.example" "Config: .env.example"
check_file "docker-compose.yml" "Config: docker-compose.yml"
check_file "INTEGRATION_APP_MODULE_EXAMPLE.ts" "Example: app.module integration"
check_file "INTEGRATION_APP_ROUTING_EXAMPLE.ts" "Example: app-routing integration"
check_file "ENVIRONMENT_EXAMPLE.ts" "Example: environment.ts"
check_file "USAGE_EXAMPLE.ts" "Example: Uso en componente"

# ============================================================
# RESUMEN
# ============================================================
echo ""
echo "============================================================"
echo "  📊 RESUMEN FINAL"
echo "============================================================"
echo ""

total=$((passed + failed))
percentage=$((passed * 100 / total))

echo -e "Archivos encontrados: ${GREEN}${passed}${NC} / ${total}"
echo -e "Archivos faltantes: ${RED}${failed}${NC} / ${total}"
echo ""
echo "Porcentaje de completitud: ${percentage}%"
echo ""

if [ $failed -eq 0 ]; then
    echo -e "${GREEN}✓ ¡VALIDACIÓN EXITOSA!${NC}"
    echo ""
    echo "🚀 Próximos pasos:"
    echo "   1. Leer QUICK_START.md"
    echo "   2. Ejecutar: cd streamTech-back && ./gradlew bootRun"
    echo "   3. Ejecutar: cd streamTech && ng serve"
    echo "   4. Ir a: http://localhost:4200/codigos"
    echo ""
else
    echo -e "${RED}✗ FALTAN ARCHIVOS${NC}"
    echo "Revisar la lista anterior para ver qué no se creó"
    echo ""
fi

echo "============================================================"
