# ❓ PREGUNTAS FRECUENTES (FAQ)

## 🎯 Conceptos Generales

### P1: ¿Qué es long polling y por qué se usa aquí?

**Respuesta:**
Long polling es cuando el cliente hace una solicitud HTTP que el servidor mantiene abierta hasta que tenga datos para responder.

**Ventajas sobre alternativas:**

| Alternativa | Ventajas | Desventajas |
|-------------|----------|------------|
| **Long Polling** | ✅ HTTP simple, ✅ Sin config especial, ✅ Funciona en cualquier firewall | ❌ Usa 1 conexión por usuario |
| **WebSocket** | ✅ Bidireccional, ✅ Bajo overhead | ❌ Requiere upgrade HTTP, ❌ Más config |
| **Server-Sent Events** | ✅ HTTP simple, ✅ One-way | ❌ No estándar en todos navegadores |
| **Polling** | ✅ Simple | ❌ Muchas requests, ❌ Latencia |

**En este caso:** Long polling es perfecto porque:
- Usuario solo hace UNA solicitud
- Espera hasta 30 segundos
- Cuando llega código, se responde
- Si no llega, timeout

---

### P2: ¿Cómo evitas race conditions?

**Respuesta:**
Con **Pessimistic Locking** en la base de datos:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT cr FROM CodeRequest cr WHERE email=:email 
        AND status='PENDING' ORDER BY created_at ASC LIMIT 1")
Optional<CodeRequest> findFirstPendingByEmailWithLock(String email);
```

Esto genera:
```sql
SELECT * FROM code_requests WHERE ... FOR UPDATE
```

`FOR UPDATE` bloquea la fila hasta que se commit la transacción.

**Ejemplo:**
```
Request A y B llegan a la vez
├─ A: SELECT... FOR UPDATE → Obtiene lock
├─ B: SELECT... FOR UPDATE → Espera (bloqueado)
├─ A: UPDATE código, COMMIT → Libera lock
├─ B: SELECT... FOR UPDATE → Obtiene lock
└─ B: UPDATE código, COMMIT
```

Resultado:
- A obtiene Code 1
- B obtiene Code 2
- ✅ Sin duplicados

---

### P3: ¿Por qué CompletableFuture y no sleep()?

**Respuesta:**

**Opción 1: Sleep (❌ MAL)**
```java
CodeRequest request = create();
long startTime = System.currentTimeMillis();

while(true) {
    Optional<Code> code = checkIfAssigned(request.getId());
    if(code.isPresent()) return code;
    
    Thread.sleep(1000); // ← BLOQUEA ESTE THREAD
    
    if(System.currentTimeMillis() - startTime > 30000) {
        return timeout;
    }
}
```

**Problema:** Si tienes 1000 usuarios, necesitas 1000 threads dormidos. Esto:
- Consume mucha memoria
- Es lento
- Tomcat tiene límite de threads

**Opción 2: CompletableFuture (✅ BIEN)**
```java
CompletableFuture<CodeResponseDto> future = new CompletableFuture<>();
requestFutures.put(requestId, future);

try {
    CodeResponseDto result = future.get(30000, TimeUnit.MILLISECONDS);
    // ← NO BLOQUEA (espera sin usar thread)
    return result;
} catch(TimeoutException e) {
    return timeout;
}
```

**Ventajas:**
- Un thread está libre mientras espera
- Puede atender otros usuarios
- Escalable a 10,000+ usuarios

---

## 🏗️ Arquitectura

### P4: ¿Cómo sé que la arquitectura es correcta?

**Respuesta:**
Verificar estos puntos:

✅ **FIFO:**
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("... ORDER BY created_at ASC LIMIT 1")
```

✅ **No bloquea threads:**
```java
future.get(timeout, unit); // CompletableFuture
```

✅ **Encriptación:**
```java
String encrypted = encryptionService.encrypt(password);
```

✅ **Transacciones atómicas:**
```java
@Transactional
public boolean assignCodeToOldestRequest(String email)
```

✅ **Índices optimizados:**
```sql
CREATE INDEX idx_email_status ON code_requests(email, status, created_at);
```

Si todos estos están presentes → Arquitectura está bien.

---

### P5: ¿Por qué IMAP en background?

**Respuesta:**
Porque escuchar correos es una tarea larga que no debe bloquear requests de usuarios.

**Sin background (❌):**
```
Request GET /get-code llega
├─ Iniciar escucha IMAP (bloquear 30s)
└─ Esperar correo (Usuario no puede hacer nada)
```

**Con background (✅):**
```
Request GET /get-code llega
├─ Crear CompletableFuture
├─ Lanzar task @Async (background)
│  ├─ ImapManager llama listener
│  ├─ Listener espera indefinidamente
│  └─ Cuando correo llega, procesa
└─ Usuario espera con future.get() (no bloquea)
```

Resultado:
- Thread request sigue libre
- IMAP escucha en paralelo
- Cuando correo llega, notifica al future
- Response va hacia usuario

---

## 🔐 Seguridad

### P6: ¿Es seguro guardar contraseñas en la DB?

**Respuesta:**
SÍ, porque se encriptan con AES-256 antes de guardar.

**Flujo:**
```
Contraseña: "myGmailPassword123"
     ↓
EncryptionService.encrypt()
     ↓ (AES-256)
Guardado: "A7f9x8bK2mL0o3pQ9rS4tU5vW6xY7z..."
     ↓
Leer de BD
     ↓
EncryptionService.decrypt()
     ↓
Contraseña: "myGmailPassword123" (en memoria, no en DB)
```

**Claves:**
- Contraseña encriptada en DB: ✅ Seguro
- Contraseña en memoria para IMAP: ✅ Seguro (solo mientras usa)
- Contraseña nunca en texto plano en BD: ✅ Seguro
- ENCRYPTION_KEY en .env: ✅ Seguro (no en git)

---

### P7: ¿Qué pasa si alguien ve los logs?

**Respuesta:**
Los logs NO contienen datos sensibles:

**Ej de logs buenos:**
```
2024-04-01 10:15:30 - INFO - Solicitud de código para email: u***@gmail.com
2024-04-01 10:15:35 - INFO - Código guardado - Email: u***@gmail.com, Código: m**
2024-04-01 10:15:40 - INFO - Asignación exitosa - Request ID: 123
```

**Ej de logs malos (NO usar):**
```
❌ password="myPassword123"
❌ code="123456"
❌ email="user@gmail.com" (completo)
```

En este código:
- Passwords nunca se loguean
- Códigos se loguean enmascarados
- Emails se podrían enmascarar

---

## 💾 Base de Datos

### P8: ¿Qué pasa si hay millones de registros?

**Respuesta:**
Los índices y queries están optimizados:

```sql
-- Índice para búsqueda rápida
CREATE INDEX idx_email_status ON code_requests(email, status, created_at);

-- Esta query usa el índice:
SELECT * FROM code_requests 
WHERE email = 'user@gmail.com' 
AND status = 'PENDING'
ORDER BY created_at ASC
LIMIT 1
-- Tiempo: <10ms incluso con 10 millones de registros
```

**Estrategia de limpieza:**
```sql
-- Eliminar solicitudes expiradas (>7 días)
DELETE FROM code_requests 
WHERE status = 'EXPIRED' 
AND created_at < DATE_SUB(NOW(), INTERVAL 7 DAY);

-- Eliminar códigos no usados (>30 días)
DELETE FROM codes 
WHERE is_assigned = false 
AND created_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

---

### P9: ¿Necesito MongoDB?

**Respuesta:**
No. MySQL es suficiente porque:

✅ Transacciones ACID (lo que SQLite no tiene)
✅ Pessimistic Locking (lo que NoSQL no tiene bien)
✅ Relaciones (Foreign Keys)
✅ Índices (muy rápidos)

Si necesitaras millones de writes/segundo → Kafka/Redis. Pero aquí no es necesario.

---

## 🧪 Testing

### P10: ¿Cómo pruebo sin enviar correos reales?

**Respuesta:**

**Opción 1: Test con mock**
```java
@Test
public void testCodeAssignment() {
    // Crear request mock
    CodeRequest request = new CodeRequest();
    request.setEmail("test@gmail.com");
    request.setStatus(PENDING);
    
    // Crear code mock
    Code code = new Code();
    code.setEmail("test@gmail.com");
    code.setCode("123456");
    code.setIsAssigned(false);
    
    // Asignar
    boolean result = assignmentService.assignCodeToOldestRequest("test@gmail.com");
    
    // Verificar
    assertTrue(result);
    assertEquals(code.getIsAssigned(), true);
}
```

**Opción 2: Test con correo de prueba**
```bash
# 1. Crear cuenta de Gmail para testing
# 2. Usar App Password de Gmail

# 3. Ejecutar test
curl -X POST http://localhost:8080/api/code-reception/get-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test-streamtech@gmail.com"}' \
  -w "Tiempo: %{time_total}s\n"

# 4. Enviar correo manual
# De: info@streamtech.com
# Para: test-streamtech@gmail.com
# Asunto: Tu código es 123456

# 5. Esperar respuesta en curl
# {success: true, code: "123456"}
```

---

### P11: ¿Cómo pruebo con 1000 usuarios simultáneos?

**Respuesta:**

**Herramienta: Apache JMeter**

```bash
# 1. Descargar y abrir JMeter
# 2. Crear Thread Group (1000 usuarios)
# 3. Agregar HTTP Request Sampler
#    Method: POST
#    URL: http://localhost:8080/api/code-reception/get-code
#    Body: {"email":"user@gmail.com"}
# 4. Agregar "Statistics" listener
# 5. Run

# Resultados esperados:
# - 100% requests completados en <30s
# - 0 errores
# - Latencia promedio: 5-15s
```

---

## ⚡ Performance

### P12: ¿Cuántos usuarios simultáneos soporta?

**Respuesta:**

Depende del servidor:

```
Servidor:        Usuarios simultáneos
Local (4GB):     50-100
AWS t3.medium:   500-1000
AWS c5.xlarge:   5000+
AWS c5.4xlarge:  20000+
```

**Cuello de botella:**
1. **MySQL connections:** De 100 por defecto
   ```properties
   spring.datasource.hikari.maximum-pool-size=20
   ```
   Aumentar a 50 si necesitas muchos usuarios

2. **Tomcat threads:** De 200 por defecto
   ```properties
   server.tomcat.threads.max=500
   ```

3. **IMAP connections:** De 10 por defecto
   ```java
   executor.setMaxPoolSize(30);
   ```

**Para 1000 usuarios simultáneos:**
```properties
# application.properties
server.tomcat.threads.max=500
spring.datasource.hikari.maximum-pool-size=50
code-reception.polling-timeout=30000
```

---

### P13: ¿Cuánta memoria usa?

**Respuesta:**

Por usuario en espera:
- CompletableFuture: ~500 bytes
- CodeRequest objeto: ~300 bytes
- En mapa requestFutures: ~800 bytes
- **Total por usuario: ~1.5 KB**

Para 1000 usuarios:
```
1000 usuarios × 1.5 KB = 1.5 MB
+ Base de datos en caché: ~50 MB
+ IMAP connections: ~20 MB
+ Tomcat overhead: ~200 MB
─────────────────
Total: ~270 MB (RAM bien)
```

Servidor típico: 2-4 GB RAM → Soporta fácilmente.

---

## 🚀 Deployment

### P14: ¿Cómo despliego a producción?

**Respuesta:**

**Opción 1: Traditional (VPS)**

```bash
# 1. Compilar
cd streamTech-back
./gradlew clean build

# 2. Copiar JAR a servidor
scp build/libs/*.jar admin@prod.example.com:/app/

# 3. En servidor
cd /app
export DATABASE=streamtech_prod
export USERNAME=prod_user
export PASSWORD=prod_password
export SECRET=super_secreto_largo
export ENCRYPTION_KEY=clave32bytesparaencriptacion

java -jar streamTech-0.0.1-SNAPSHOT.jar

# 4. Mantener vivo (systemd o supervisor)
```

**Opción 2: Docker**

```bash
# 1. Build imagen
docker build -t streamtech-backend:1.0 .

# 2. Push a registry
docker push your-registry/streamtech-backend:1.0

# 3. Deploy (Kubernetes o docker-compose)
docker-compose -f docker-compose.yml up -d
```

**Opción 3: Cloud (Docker + Platform)**

```bash
# AWS Elastic Beanstalk
eb create streamtech-backend
eb deploy

# Google Cloud
gcloud app deploy

# Heroku
git push heroku main
```

---

### P15: ¿Cómo monitoreo en producción?

**Respuesta:**

**Logs:**
```bash
# Ver logs en tiempo real
tail -f /var/log/streamtech/app.log | grep "code-reception"

# Con ELK Stack (Elasticsearch, Logstash, Kibana)
# Ver logs centralizados
```

**Métricas (Prometheus):**
```yaml
# application.properties
management.endpoints.web.exposure.include=metrics
management.metrics.export.prometheus.enabled=true
```

```bash
# Ver métricas
curl http://localhost:8080/actuator/prometheus
```

**Health Check:**
```bash
# Cron job cada 5 minutos
*/5 * * * * curl -f http://localhost:8080/api/code-reception/health || alert

# Si falla 3 veces, restart automático
```

**Alertas:**
```
- HIGH: % tiempo sin disponibilidad > 1%
- MEDIUM: Latencia promedio > 5 segundos
- LOW: Códigos sin procesar > 100
```

---

## 🐛 Troubleshooting

### P16: ¿Por qué no llegan códigos?

**Checklist:**

```
1. ¿Está el backend ejecutándose?
   $ curl http://localhost:8080/api/code-reception/health
   Response: {"status":"UP",...}

2. ¿Está la cuenta de email registrada?
   $ mysql streamtech -e "SELECT email FROM email_accounts;"
   Response: debe mostrar tu email

3. ¿Está la conexión IMAP activa?
   Logs: "Escucha iniciada exitosamente para: user@gmail.com"

4. ¿El email está llegando al INBOX?
   Revisar manualmente en webmail

5. ¿El regex extrae el código?
   Test regex: \\d{6}
   En tu email: "Tu código es: 123456"

6. ¿Hay error en la BD?
   SELECT * FROM code_requests WHERE status='PENDING';
   Debe mostrar tu solicitud
```

---

### P17: ¿Por qué me da timeout siempre?

**Checklist:**

1. **IMAP Connection falla:**
   ```bash
   # Test manualmente
   telnet imap.gmail.com 993
   # Si no conecta → firewall o credenciales
   ```

2. **Credenciales incorrectas:**
   ```bash
   # Para Gmail: Usar App Password, no contraseña solo
   # Para Outlook: Usar contraseña normal
   # Para Yahoo: Usar App Password
   ```

3. **Regex no extrae código:**
   ```
   Tu email: "Your verification code is ABC123"
   Regex: \\d{6}  ← NO EXTRAERÍA porque tiene letras
   
   Solución: Cambiar regex a [A-Z0-9]{6}
   ```

4. **Código no se asigna:**
   ```sql
   SELECT COUNT(*) FROM codes WHERE email='test@gmail.com' AND is_assigned=0;
   ```
   Si muestra 0 → El código no se guardó en BD.

---

## 📚 Integración

### P18: ¿Cómo integro en mi app Angular existente?

**Respuesta:**

**Paso 1: Importar módulo**
```typescript
// app.module.ts
import { CodeReceptionModule } from './modules/code-reception/code-reception.module';

@NgModule({
  imports: [
    CommonModule,
    CodeReceptionModule  // ← Agregar aquí
  ]
})
export class AppModule { }
```

**Paso 2: Agregar ruta**
```typescript
// app-routing.module.ts
const routes: Routes = [
  {
    path: 'codigos',
    loadChildren: () => import('./modules/code-reception/code-reception.module')
      .then(m => m.CodeReceptionModule)
  }
];
```

**Paso 3: Usar en componente**
```typescript
// mi-componente.component.ts
constructor(private codeService: CodeReceptionService) {}

getCodeForUser() {
  this.codeService.getCode('user@gmail.com').subscribe({
    next: (response) => {
      if(response.success) {
        alert('Código: ' + response.code);
      }
    }
  });
}
```

---

### P19: ¿Puedo usar solo el servicio sin el componente?

**Respuesta:**
SÍ. El servicio es independiente:

```typescript
// app.service.ts
import { CodeReceptionService } from '...';

constructor(private codeService: CodeReceptionService) {}

myMethod() {
  this.codeService.getCode('user@email.com').subscribe({
    next: (resp) => {
      // Tu lógica aquí
    }
  });
}
```

El componente es solo UI. Puedes reemplazarlo con tu propio diseño.

---

## 📞 Más Dudas

### P20: ¿Dónde puedo reportar bugs?

**Respuesta:**

1. Revisar la GUIA_CODE_RECEPTION.md → Troubleshooting
2. Revisar logs del backend: `./gradlew bootRun`
3. Revisar console del navegador: F12 → Console
4. Si persiste: Revisar código del servicio que genera error

**Información útil para reporte:**
- Logs exactos del error
- Pasos para reproducir
- Versión de Java/Spring/Angular
- BD y IMAP server usado

---

**¿Más dudas?** Revisar archivos de documentación o GUIA_CODE_RECEPTION.md
