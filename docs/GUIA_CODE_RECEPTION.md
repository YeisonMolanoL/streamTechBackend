# 📖 GUÍA DE IMPLEMENTACIÓN - SISTEMA DE RECEPCIÓN DE CÓDIGOS

## 📋 Índice

1. [Descripción General](#descripción-general)
2. [Arquitectura](#arquitectura)
3. [Setup Base de Datos](#setup-base-de-datos)
4. [Setup Backend](#setup-backend)
5. [Setup Frontend](#setup-frontend)
6. [Configuración IMAP](#configuración-imap)
7. [Testing](#testing)
8. [Troubleshooting](#troubleshooting)

---

## 📝 Descripción General

Sistema completo para centralizar la recepción de códigos enviados por correo electrónico con:

✅ **Long polling** en backend (30 segundos timeout)
✅ **FIFO** con bloqueo pesimista (sin race conditions)
✅ **IMAP** para escuchar correos en tiempo real
✅ **Concurrencia** soportada (múltiples usuarios, mismo correo)
✅ **Encriptación** AES-256 para contraseñas
✅ **No servicios externos** pagos

---

## 🏗️ Arquitectura

### Backend (Spring Boot)

```
streamTech-back/
├── src/main/java/com/TechPulseInnovations/streamTech/codeReception/
│   ├── controllers/       # Endpoints REST
│   ├── entities/          # Modelos JPA
│   ├── repositories/      # Data access layer
│   ├── services/          # Lógica de negocio
│   │   ├── EncryptionService.java
│   │   ├── ImapManagerService.java
│   │   ├── CodeAssignmentService.java
│   │   └── LongPollingService.java
│   ├── dtos/             # Modelos de dato para API
│   └── config/           # Configuración Spring
└── resources/
    ├── application.properties
    └── sql/code-reception-schema.sql
```

**Flujo:**

```
Cliente Angular
       ↓
   POST /get-code
       ↓
LongPollingService (espera max 30s)
       ↓
CodeRequestRepository (PESSIMISTIC_LOCK)
       ↓
ImapManagerService (escucha IMAP en background)
       ↓
Correo llega → se extrae código con regex
       ↓
CodeAssignmentService (FIFO, bloqueo pesimista)
       ↓
Devuelve código a cliente
```

### Frontend (Angular)

```
streamTech/src/app/modules/code-reception/
├── components/
│   ├── code-reception.component.ts
│   ├── code-reception.component.html
│   └── code-reception.component.css
├── services/
│   └── code-reception.service.ts
├── code-reception.module.ts
└── code-reception-routing.module.ts
```

### Base de Datos (MySQL)

```
email_accounts
├── id (PK)
├── email
├── password_encrypted (AES-256)
├── host (IMAP server)
├── port
├── secure (TLS/SSL)
└── is_active

code_requests
├── id (PK)
├── email
├── status (PENDING, ASSIGNED, EXPIRED)
├── created_at
├── assigned_code_id (FK)
└── assigned_at

codes
├── id (PK)
├── email
├── code
├── is_assigned (BOOLEAN)
├── created_at
├── assigned_to_request_id
└── assigned_at

parsing_rules
├── id (PK)
├── service (Google, Microsoft, etc)
├── regex_pattern
└── is_active
```

---

## 🗄️ Setup Base de Datos

### 1. Crear base de datos

```sql
CREATE DATABASE streamtech CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE streamtech;
```

### 2. Ejecutar script de schema

```bash
mysql -u root -p streamtech < streamTech-back/src/main/resources/sql/code-reception-schema.sql
```

### 3. Verificar tablas

```sql
SHOW TABLES;
DESC email_accounts;
DESC code_requests;
DESC codes;
DESC parsing_rules;
```

### 4. Insertar datos iniciales (opcional)

```sql
-- Ejemplo: agregar una cuenta de Gmail
INSERT INTO email_accounts 
(email, password_encrypted, host, port, secure, is_active) 
VALUES 
('tu-email@gmail.com', '[ENCRYPTED_PASSWORD]', 'imap.gmail.com', 993, true, true);
```

---

## 🔧 Setup Backend

### 1. Dependencias en build.gradle

✅ **Ya actualizadas** (descomentar si es necesario):

```gradle
implementation 'org.springframework.boot:spring-boot-starter-mail'
implementation 'com.sun.mail:javax.mail:1.6.2'
implementation 'org.springframework.security:spring-security-crypto'
```

### 2. Variables de entorno

Crear `.env` o configurar en IDE:

```properties
DATABASE=streamtech
USERNAME=root
PASSWORD=tu_contraseña
SECRET=tu_secret_jwt_largo
ENCRYPTION_KEY=clave32bytesparaaes256encryption
```

En `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/${DATABASE}
spring.datasource.username=${USERNAME}
spring.datasource.password=${PASSWORD}
encryption.key=${ENCRYPTION_KEY}
```

### 3. Compilar y ejecutar

```bash
# Compilar
./gradlew build

# Ejecutar
./gradlew bootRun

# O en IDE: Run -> Run 'StreamTechApplication'
```

### 4. Verificar que funciona

```bash
curl http://localhost:8080/api/code-reception/health

# Output esperado:
# {
#   "status": "UP",
#   "service": "code-reception",
#   "activeEmails": 0
# }
```

---

## 🎨 Setup Frontend

### 1. Importar módulo en app.module.ts

```typescript
import { CodeReceptionModule } from './modules/code-reception/code-reception.module';

@NgModule({
  imports: [
    // ... otros módulos
    CodeReceptionModule
  ]
})
export class AppModule { }
```

### 2. Agregar ruta en app-routing.module.ts

```typescript
const routes: Routes = [
  {
    path: 'codigos',
    loadChildren: () => import('./modules/code-reception/code-reception.module')
      .then(m => m.CodeReceptionModule)
  },
  // ... otras rutas
];
```

### 3. Configurar base URL de API

En `code-reception.service.ts`:

```typescript
private apiUrl = 'http://localhost:8080/api/code-reception';
// O usar variable de entorno
private apiUrl = `${environment.apiUrl}/api/code-reception`;
```

### 4. Ejecutar frontend

```bash
# En carpeta streamTech/
ng serve

# Acceder en http://localhost:4200/codigos
```

---

## 📧 Configuración IMAP

### Gmail

```typescript
{
  email: "tu-email@gmail.com",
  host: "imap.gmail.com",
  port: 993,
  secure: true,
  password: "tu_app_password" // No contraseña normal, usar App Password
}
```

**Pasos para Gmail:**
1. Habilitar 2FA: https://myaccount.google.com/security
2. Generar App Password: https://myaccount.google.com/apppasswords
3. Usar esa contraseña en la configuración

### Outlook/Hotmail

```typescript
{
  email: "tu-email@hotmail.com",
  host: "imap-mail.outlook.com",
  port: 993,
  secure: true,
  password: "tu_contraseña"
}
```

### Yahoo Mail

```typescript
{
  email: "tu-email@yahoo.com",
  host: "imap.mail.yahoo.com",
  port: 993,
  secure: true,
  password: "tu_app_password"
}
```

### Custom (tu servidor)

```typescript
{
  email: "usuario@tudominio.com",
  host: "tu_servidor_imap_host",
  port: 993,
  secure: true,
  password: "tu_contraseña"
}
```

### Agregar cuenta vía API

```bash
curl -X POST http://localhost:8080/api/code-reception/admin/add-email-account \
  -H "Content-Type: application/json" \
  -d '{
    "email": "tu-email@gmail.com",
    "password": "tu_app_password",
    "host": "imap.gmail.com",
    "port": 993,
    "secure": true
  }'
```

---

## 🧪 Testing

### Test 1: Health check

```bash
curl http://localhost:8080/api/code-reception/health
```

### Test 2: Agregar cuenta de email

```bash
curl -X POST http://localhost:8080/api/code-reception/admin/add-email-account \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "host": "imap.gmail.com",
    "port": 993,
    "secure": true
  }'
```

### Test 3: Solicitar código (con curl)

```bash
curl -X POST http://localhost:8080/api/code-reception/get-code \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com"}' \
  -w "Tiempo: %{time_total}s\n"
```

**Esperar respuesta:**
- Si llegó código: `{"success":true,"code":"123456"}`
- Si timeout: `{"success":false,"message":"No llegó ningún código"}`

### Test 4: Desde Angular (navegador)

1. Ir a `http://localhost:4200/codigos`
2. Ingresa tu email
3. Click en "Obtener Código"
4. Envía un correo con código a la cuenta configurada
5. El código debe aparecer en la página en máximo 30 segundos

---

## 🔒 Seguridad

### 1. Encriptación de contraseñas

Todos los passwords IMAP se encriptan con AES-256 antes de guardarse:

```java
String encryptedPassword = encryptionService.encrypt(plainPassword);
// Se guarda: encryptedPassword en BD
// Se descifra cuando se usa para IMAP
```

### 2. CORS configurado

Solo acepta requests de:
- `http://localhost:4200`
- `http://localhost:8080`

Editar en `WebConfig.java`:

```java
registry.addMapping("/api/**")
    .allowedOrigins("http://localhost:4200", "http://localhost:8080")
    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
```

### 3. Variables en environment

Nunca hardcodear:
- DATABASE passwords
- JWT secrets
- ENCRYPTION_KEY

---

## 🐛 Troubleshooting

### Problema: No se conecta a IMAP

**Síntomas:**
- Logs: "Error creando conexión IMAP"
- No llegan códigos

**Soluciones:**

```bash
# 1. Verificar credenciales
# - ¿Password es correcto?
# - ¿No tiene 2FA habilitado sin usar App Password?

# 2. Verificar host/puerto
# - ¿Firewall bloquea IMAP?
# - ¿Puerto 993 abierto?

# 3. Debug - agregar logs
# En ImapManagerService.java:
log.debug("Intentando conectar a {}:{}", host, port);
```

### Problema: Códigos no se asignan

**Síntomas:**
- Los códigos se guardan pero nunca se devuelven
- Las solicitudes expiran con timeout

**Causas comunes:**

```java
// 1. El email no coincide exactamente
// Solución: normalizar a lowercase
code.setEmail(email.trim().toLowerCase());

// 2. Regex no extrae el código
// Solución: agregar más patrones en ImapManagerService.extractCode()

// 3. No hay base de datos
// Solución: ejecutar script SQL
```

### Problema: Errores de concurrencia

Si ves: `LockTimeoutException`, aumentar timeout en MySQL:

```sql
SET GLOBAL innodb_lock_wait_timeout = 50;
```

### Problema: Memoria se llena

Si los listeners no se limpian:

```java
// Descomentar en ImapManagerService.stopListeningForEmail()
inbox.removeMessageCountListener(listener);
```

### Logs útiles

```bash
# Ejecutar backend con mejor logging
./gradlew bootRun --args='--logging.level.com.TechPulseInnovations=DEBUG'

# Seguir logs en tiempo real
tail -f build/logs/spring.log | grep "code-reception"
```

---

## 📊 Monitoreo

### Métricas importantes

```sql
-- Códigos pendientes
SELECT COUNT(*) as pendientes FROM codes WHERE is_assigned = false;

-- Solicitudes activas
SELECT COUNT(*) as activas FROM code_requests WHERE status = 'PENDING';

-- Cuentas de email activas
SELECT COUNT(*) as activas FROM email_accounts WHERE is_active = true;

-- Tiempo promedio de asignación
SELECT AVG(TIMESTAMPDIFF(SECOND, created_at, assigned_at)) as promedio
FROM codes WHERE assigned_at IS NOT NULL;
```

### Endpoint de health

```bash
# Verificar servicio cada 30 segundos
watch -n 30 'curl -s http://localhost:8080/api/code-reception/health | json_pp'
```

---

## 🚀 Deployment

### 1. Build JAR

```bash
./gradlew bootJar

# JAR generado en:
# build/libs/streamTech-0.0.1-SNAPSHOT.jar
```

### 2. Ejecutar JAR

```bash
java -Dspring.profiles.active=production \
     -DDATABASE=streamtech_prod \
     -DUSERNAME=prod_user \
     -DPASSWORD=prod_password \
     -DSECRET=muy_largo_y_seguro \
     -DENCRYPTION_KEY=clave32bytesparaaes256encryption \
     -jar build/libs/streamTech-0.0.1-SNAPSHOT.jar
```

### 3. Docker (opcional)

```dockerfile
FROM openjdk:17-jdk-slim
COPY build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
EXPOSE 8080
```

```bash
docker build -t streamtech-backend .
docker run -e DATABASE=streamtech -p 8080:8080 streamtech-backend
```

---

## 📞 Soporte

Para dudas o errores:

1. Revisar logs del backend: `./gradlew bootRun`
2. Revisar console del navegador (F12)
3. Verificar conexión IMAP con telnet:
   ```bash
   telnet imap.gmail.com 993
   ```
4. Verificar base de datos:
   ```sql
   SELECT * FROM code_requests ORDER BY created_at DESC LIMIT 5;
   SELECT * FROM codes ORDER BY created_at DESC LIMIT 5;
   ```

---

**¡Sistema listo para usar! 🎉**
