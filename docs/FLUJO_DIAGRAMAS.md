# 📊 DIAGRAMA DE FLUJO - SISTEMA RECEPCIÓN DE CÓDIGOS

## 🔄 Flujo Completo (30 segundos max)

```
┌─────────────────────────────────────────────────────────────────┐
│                    TIEMPO 0s (Usuario Activa)                    │
└─────────────────────────────────────────────────────────────────┘

    ┌──────────────────┐
    │  Angular (4200)  │
    │                  │
    │ Input: Email     │
    │ Click: Obtener   │
    └────────┬─────────┘
             │
             │ POST /get-code
             │ Content-Type: application/json
             │ { email: "user@gmail.com" }
             │
             ▼
    ┌──────────────────────────────────────────┐
    │    Spring Boot Backend (8080)            │
    │                                          │
    │ CodeReceptionController.getCode()       │
    │ ├─ Validar email                        │
    │ └─ Llamar LongPollingService            │
    └────────┬─────────────────────────────────┘
             │
             ▼
    ┌──────────────────────────────────────────┐
    │   LongPollingService.waitForCode()       │
    │                                          │
    │ 1. Crear CodeRequest(status=PENDING)    │
    │    └─ Save DB                           │
    │                                          │
    │ 2. Crear CompletableFuture               │
    │    └─ requestFutures.put(id, future)    │
    │                                          │
    │ 3. Iniciar ImapManager (background)     │
    │    └─ @Async startListeningForEmail()   │
    │                                          │
    │ 4. future.get(timeout=30000ms)           │
    │    └─ ESPERAR SIN BLOQUEAR THREAD       │
    └────────┬─────────────────────────────────┘
             │
             │ (En paralelo)
             │
     ┌───────┴──────────────────────┬──────────────────────────┐
     │                              │                          │
     ▼                              ▼                          ▼
┌──────────┐                  ┌──────────────┐          ┌────────────┐
│ ESPERANDO│                  │ CORREO LLEGA │          │ TIMEOUT 30s│
│ CÓDIGO   │                  │              │          │            │
└──────────┘                  └──────┬───────┘          └────┬───────┘
                                     │                       │
                                     ▼                       │
                          ┌──────────────────────┐          │
                          │  ImapManagerService  │          │
                          │  processMessage()    │          │
                          │                      │          │
                          │ 1. parseEmail()      │          │
                          │ 2. extractCode()     │          │
                          │    └─ regex: \\d{6} │          │
                          │ 3. saveDB()          │          │
                          │    └─ Code(email...) │          │
                          │       is_assigned=NO │          │
                          └──────┬───────────────┘          │
                                 │                          │
                                 ▼                          │
                     ┌────────────────────────────┐         │
                     │ CodeAssignmentService      │         │
                     │ assignCodeToOldestRequest()│         │
                     │                            │         │
                     │ @Transactional             │         │
                     │ @Lock(PESSIMISTIC_WRITE)   │         │
                     │                            │         │
                     │ 1. SELECT cr FROM...       │         │
                     │    WHERE status=PENDING    │         │
                     │    FOR UPDATE ← LOCK      │         │
                     │                            │         │
                     │ 2. SELECT c FROM...        │         │
                     │    WHERE is_assigned=NO    │         │
                     │    FOR UPDATE ← LOCK      │         │
                     │                            │         │
                     │ 3. UPDATE codes:           │         │
                     │    is_assigned = true      │         │
                     │    assigned_at = NOW()    │         │
                     │                            │         │
                     │ 4. UPDATE code_requests:   │         │
                     │    status = ASSIGNED       │         │
                     │    assigned_code_id = id   │         │
                     │                            │         │
                     │ 5. Notificar              │         │
                     │    longPollingService.    │         │
                     │    notifyCodeAssigned()   │         │
                     └──────┬────────────────────┘         │
                            │                              │
                            ▼                              │
                     ┌─────────────────┐                   │
                     │ CompletableFuture│                  │
                     │ .complete(resp) │                  │
                     │ {               │                  │
                     │   success: true │                  │
                     │   code: "123456"│                  │
                     │   ...           │                  │
                     │ }               │                  │
                     └──────┬──────────┘                   │
                            │                              │
                            │ (Respuesta en <30s)         │
                            │                              │
                            ▼                              │
                     ┌─────────────────┐                   │
                     │ HTTP Response   │                   │
                     │ 200 OK          │◄──────────────────┘
                     │ {               │  (Timeout)
                     │   success: true │  Response:
                     │   code: "123456"│  {
                     │ }               │    success: false
                     └──────┬──────────┘    message: "..."
                            │               }
                            ▼
                     ┌─────────────────────┐
                     │ Angular recibe      │
                     │ - success=true      │
                     │ - code="123456"     │
                     │                     │
                     │ UI muestra código   │
                     │ ✓ Código obtenido   │
                     │ 123456              │
                     │ [Copiar]            │
                     └─────────────────────┘
```

---

## 🔐 FIFO Lock (Sección Crítica)

```
┌─────────────────────────────────────────────────────────┐
│              Múltiples Solicitudes Simultáneas           │
└─────────────────────────────────────────────────────────┘

Request A: user1@gmail.com (t=0s)     Request B: user1@gmail.com (t=0.1s)
│                                      │
├─ Llega a CodeAssignmentService       │
│  ├─ SELECT code_requests             │
│  │  WHERE email=user1@gmail.com      │
│  │  FOR UPDATE ← LOCK ADQUIRIDO      │
│  │                                   │
│  │  Ahora Request B debe esperar     ├─ Llega a CodeAssignmentService
│  │                                   │  ├─ SELECT code_requests  
│  │ ├─ SELECT codes                   │  │  WHERE email=user1@gmail.com
│  │ │  WHERE email=user1@gmail.com,   │  │  FOR UPDATE ← ESPERA (blocking)
│  │ │  is_assigned=NO                 │  │
│  │ │  FOR UPDATE ← LOCK              │  │
│  │                                   │  │
│  │ ├─ UPDATE codes SET...            │  │
│  │ │ (Asigna Code ID 5)              │  │
│  │                                   │  │
│  │ ├─ UPDATE code_requests SET...    │  │
│  │ │ (Asigna Code ID 5 a Request A)  │  │
│  │                                   │  │
│  └─ COMMIT ← LOCK LIBERADO           │  │
│     Transacción exitosa               │  │
│                                       │  ├─ SELECT code_requests (LOCK ADQUIRIDO AHORA)
│                                       │  │  Obtiene siguiente código no asignado
│                                       │  │  (Code ID 6)
│                                       │  │
│                                       │  ├─ UPDATE codes SET...
│                                       │  │
│                                       │  ├─ UPDATE code_requests SET...
│                                       │  │
│                                       │  └─ COMMIT ← LOCK LIBERADO
│                                       │     Transacción exitosa
│
│       Posible sin Bloqueo:            │
│  A. Request A obtiene Code 5          │       RACE CONDITION:
│  B. Request B obtiene Code 5          │  ❌ Ambos usan Code 5
│     ↓                                │
│     Desastre                         │
│
│       Con Pessimistic Locking:        │
│  A. Request A LOCK (waiting B)        │       ✅ ORDENADO:
│  B. Request B espera a A              │  A. Request A obtiene Code 5
│  A. libera LOCK                       │  B. Request B obtiene Code 6
│  B. adquiere LOCK                     │     ✓ FIFO garantizado
│  ↓                                   │
│     Correcto - FIFO                 │
```

---

## 🧵 Thread Pool (CompletableFuture)

```
┌───────────────────────────────────────────────────────────┐
│        Thread Pool - Spring Boot Default                   │
│        (Sin bloqueos)                                      │
└───────────────────────────────────────────────────────────┘

Tomcat ThreadPool: ─────────────────────────────────────────
│ Thread 1 ┬─ Usuario A (esperando código)
│          │  └─ future.get(30000ms) ← NO BLOQUEA
│          │     (CompletableFuture)
│          ▼
│ 
│ Thread 2 ┬─ Usuario B (esperando código)
│          │  └─ future.get(30000ms) ← NO BLOQUEA
│          │     (CompletableFuture)
│          ▼
│
│ Thread 3 ┬─ Usuario C (esperando código)
│          │  └─ future.get(30000ms) ← NO BLOQUEA
│          │     (CompletableFuture)
│          ▼
│
│ Thread 4 ┬─ Usuario D (esperando código)
│          │  └─ future.get(30000ms) ← NO BLOQUEA
│          │     (CompletableFuture)
│          ▼
│
│         ...
│

IMAP ThreadPool: ────────────────────────────────────────
│ imap-1 ┬─ Escuchando gmail.com
│        │  (MessageCountListener - IDLE)
│        ▼
│
│ imap-2 ┬─ Escuchando outlook.com
│        │  (MessageCountListener - IDLE)
│        ▼
│
│ imap-3 ┬─ Escuchando yahoo.com
│        │  (MessageCountListener - IDLE)
│        ▼

RESULTADO:
✓ Múltiples usuarios esperan simultáneamente
✓ Múltiples IMAP listeners escuchan
✓ Threads no se bloquean
✓ Pool no se agota
✓ Escalable a 1000+ usuarios
```

---

## 📧 IMAP Flow (Background)

```
┌───────────────────────────────────────────────────────────┐
│              IMAP Manager - Background Thread              │
└───────────────────────────────────────────────────────────┘

t=0s   ┌──────────────────────────────────┐
       │ imapManagerService               │
       │ .startListeningForEmail("user... │
       └────────┬─────────────────────────┘
                │
       t=5s     ▼
       ┌──────────────────────────────────┐
       │ Store store = new ImapConnection │
       │  host: imap.gmail.com            │
       │  port: 993 (SSL)                 │
       │  timeout: 10s                    │
       │                                  │
       │ activeStores.put(email, store)   │
       │ (Conexión persistente abierta)   │
       └────────┬─────────────────────────┘
                │
       t=10s    ▼
       ┌──────────────────────────────────┐
       │ Folder inbox = store.getFolder() │
       │ inbox.open(READ_WRITE)           │
       │                                  │
       │ ImapListener listener = new...   │
       │ inbox.addMessageCountListener()  │
       │                                  │
       │ Esperando mensajes...            │
       │ (IDLE push si el servidor soporta)
       │ (polling cada pocos segundos si no)
       └────────┬─────────────────────────┘
                │
       t=15s    │  (Usuario espera código)
       t=16s    │
       t=17s    │
       t=18s    │
       t=19s    ▼
       ┌──────────────────────────────────┐
       │ ¡CORREO LLEGA!                   │
       │                                  │
       │ messagesAdded()                  │
       │ └─ ImapListener activado         │
       │                                  │
       │ process_message:                 │
       │  ├─ subject: "Tu código"         │
       │  ├─ content: "Tu código es: 12..." │
       │  ├─ parseEmail()                 │
       │  ├─ extractCode()                │
       │  │   ├─ Aplicar regex             │
       │  │   └─ Extraído: "123456"       │
       │  ├─ Code.builder()               │
       │  │   .email("user@...")          │
       │  │   .code("123456")             │
       │  │   .isAssigned(false)          │
       │  │   .build()                    │
       │  ├─ codeRepository.save()        │
       │  │   └─ INSERT INTO codes (...)  │
       │  └─ assignmentService.assign()   │
       │      └─ (ver diagrama FIFO)      │
       │                                  │
       │ Notificar al futuro:             │
       │ future.complete(response)        │
       └────────┬─────────────────────────┘
                │
       t=19.5s  ▼
       ┌──────────────────────────────────┐
       │ Usuario recibe código            │
       │ (Tiempo total: <20s)             │
       │                                  │
       │ Resto del tiempo (20-30s):       │
       │ El futuro ya completado,         │
       │ espera termina                   │
       └──────────────────────────────────┘
```

---

## 🗄️ BASE DE DATOS (Transacciones)

```
┌───────────────────────────────┐                ┌───────────────────────────────┐
│  Transacción A                │                │  Transacción B                │
│  (CodeAssignmentService)      │                │  (CodeAssignmentService)      │
└───────────────────────────────┘                └───────────────────────────────┘

t=0s  ├─ BEGIN TRANSACTION          
      │                            t=0.01s  ├─ BEGIN TRANSACTION
      │
      ├─ SELECT CodeRequest         
      │  FOR UPDATE (LOCK)
      │  ✓ Obtiene lock para
      │    Request 1                │          ├─ SELECT CodeRequest
      │                             │          │  FOR UPDATE (lock)
      │                             │          │  ⏳ Espera (A tiene lock)
      │
      ├─ SELECT Code
      │  FOR UPDATE                 │
      │  ✓ Obtiene lock             │
      │                             │
      ├─ UPDATE Code SET
      │  is_assigned=true
      │  assigned_at=NOW()          │
      │  assigned_to_request_id=1   │
      │                             │
      ├─ UPDATE CodeRequest SET     │
      │  status='ASSIGNED'          │
      │  assigned_code_id=5         │
      │                             │
      ├─ COMMIT ← LOCKS LIBERADOS   │
      │  ✓ Transacción A completa   │
      │                             │          │  (Ahora obtiene lock)
      │                             │          │  ✓ Obtiene lock para
      │                             │          │    Request 2
      │                             │          │
      │                             │          ├─ SELECT Code
      │                             │          │  FOR UPDATE
      │                             │          │  ✓ Diferente Code (ID 6)
      │                             │          │    (porque 5 ya asignado)
      │                             │          │
      │                             │          ├─ UPDATE Code(6)
      │                             │          ├─ UPDATE CodeRequest(2)
      │                             │          ├─ COMMIT
      │                             │          │  ✓ Transacción B completa

RESULTADO:
✓ Request 1 → Code 5
✓ Request 2 → Code 6
✓ Sin duplicados
✓ Sin pérdidas
✓ FIFO garantizado
```

---

## 📊 Estado Final en BD

```
┌─────────────────────────────────────────────────────────┐
│  Tabla: code_requests                                   │
├──────┬──────────────────┬──────────┬──────────────────┤
│ id   │ email            │ status   │ assigned_code_id │
├──────┼──────────────────┼──────────┼──────────────────┤
│ 1    │ user@gmail.com   │ ASSIGNED │ 5                │
│ 2    │ user@gmail.com   │ ASSIGNED │ 6                │
│ 3    │ user@gmail.com   │ PENDING  │ NULL             │
│ 4    │ other@outlook... │ ASSIGNED │ 8                │
│ 5    │ user@gmail.com   │ EXPIRED  │ NULL             │
└──────┴──────────────────┴──────────┴──────────────────┘

┌──────────────────────────────────────────────────────────┐
│  Tabla: codes                                            │
├──────┬──────────────────┬─────────┬────────────┬────────┤
│ id   │ email            │ code    │ is_assigned│ assigned│
├──────┼──────────────────┼─────────┼────────────┼────────┤
│ 5    │ user@gmail.com   │ 123456  │ 1          │ req 1  │
│ 6    │ user@gmail.com   │ 654321  │ 1          │ req 2  │
│ 7    │ user@gmail.com   │ 999999  │ 0          │ NULL   │
│ 8    │ other@outlook... │ 555555  │ 1          │ req 4  │
└──────┴──────────────────┴─────────┴────────────┴────────┘

Métricas:
├─ Total requests: 5
├─ Asignados: 4
├─ Pendientes: 1
├─ Expirados: 0
├─ Códigos sin usar: 1
└─ Tasa de éxito: 80%
```

---

## ⏱️ Tiempos

```
Usuario espera:
0s ─────────────────────────────────────────→ 30s (timeout)
   ∧                       ∧
   │                       │
   Request                Respuesta<20s usualmente
   
Email llega y se procesa:
┌─ 1-2s: Transporte email
├─ 1s: IMAP detecta
├─ 1s: Procesar mensaje + regex
├─ 1s: Asignación en DB (con lock)
└─ 2-3s: Completar CompletableFuture

Total: <10-15s en caso exitoso
