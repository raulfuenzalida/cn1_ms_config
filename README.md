# MS-Config

Microservicio de configuración de costos para PrintWorks. Este servicio es responsable de administrar la configuración de costos utilizada por el sistema, incluyendo filamentos, precios de electricidad y consumo de impresoras.

## Tecnologías

- Java 21
- Spring Boot 3.4.11
- Maven
- Spring Data JPA
- Spring Security con OAuth2 Resource Server
- MySQL 8.0
- Lombok
- SpringDoc OpenAPI (Swagger)
- JUnit 5
- Docker

## Características

- Gestión de filamentos (crear, listar, actualizar, cambiar estado)
- Configuración de costos energéticos (electricidad y consumo de impresoras)
- API REST versionada bajo `/api/v1/config`
- Autenticación mediante OAuth2 Resource Server (JWT Bearer Token)
- Documentación API con Swagger/OpenAPI
- Validación de datos con Jakarta Validation
- Manejo centralizado de excepciones
- Soporte para CORS
- Tests unitarios y de integración
- Contenedor Docker con docker-compose

## Arquitectura

El microservicio sigue la arquitectura estándar de Spring Boot:

```
Controller -> Service -> Repository -> Model
```

Las entidades JPA nunca se exponen directamente; se utilizan DTOs para las solicitudes y respuestas.

## Endpoints

### Filamentos

- `GET /api/v1/config/filaments` - Obtener todos los filamentos
- `GET /api/v1/config/filaments/{id}` - Obtener filamento por ID
- `POST /api/v1/config/filaments` - Crear nuevo filamento
- `PUT /api/v1/config/filaments/{id}` - Actualizar filamento existente
- `PATCH /api/v1/config/filaments/{id}/status` - Actualizar estado de filamento

### Configuración de Impresión

- `GET /api/v1/config/printing` - Obtener configuración de impresión
- `PUT /api/v1/config/printing` - Actualizar configuración de impresión

Todos los endpoints requieren autenticación mediante JWT Bearer Token.

## Configuración

### Variables de Entorno

El servicio utiliza las siguientes variables de entorno (ver `.env.example`):

- `SERVER_PORT` - Puerto del servidor (default: 8080)
- `DB_HOST` - Host de base de datos MySQL
- `DB_PORT` - Puerto de base de datos MySQL (default: 3306)
- `DB_USER` - Usuario de base de datos
- `DB_PASSWORD` - Contraseña de base de datos
- `JWT_ISSUER_URI` - URI del emisor de tokens OAuth2

### Base de Datos

El esquema de base de datos se encuentra en `database/init.sql` y se inicializa automáticamente al iniciar el servicio.

#### Tablas

- `filaments` - Almacena información de filamentos de impresión 3D
- `printing_config` - Almacena configuración de costos energéticos

## Instalación y Ejecución

### Requisitos Previos

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Docker (opcional, para ejecución con contenedores)

### Ejecución Local

1. Clonar el repositorio
2. Configurar las variables de entorno en `.env` (basado en `.env.example`)
3. Ejecutar con Maven:

```bash
mvn spring-boot:run
```

### Ejecución con Docker

1. Construir la imagen Docker:

```bash
docker build -t ms-config .
```

2. Ejecutar con docker-compose:

```bash
docker-compose up -d
```

### Ejecución de Tests

```bash
mvn test
```

### Build del Proyecto

```bash
mvn clean package
```

## Documentación API

La documentación Swagger está disponible en:

```
http://localhost:8080/swagger-ui.html
```

## Seguridad

- El servicio utiliza OAuth2 Resource Server para validar tokens JWT
- Los endpoints protegidos requieren un Access Token válido en el header `Authorization: Bearer <token>`
- El ID Token no es aceptado como sustituto del Access Token
- CORS está configurado para permitir orígenes específicos (configurable en producción)

## Tests

El proyecto incluye:

- Tests unitarios de servicios (Mockito)
- Tests de seguridad de API (Spring Security Test)
- Tests de validación de DTOs
- Tests de integración con H2 Database

## Estructura del Proyecto

```
ms-config/
├── database/
│   └── init.sql
├── src/main/java/duoc/cn1/ms_config/
│   ├── MsConfigApplication.java
│   ├── config/
│   │   └── OpenApiConfig.java
│   ├── controller/
│   │   ├── FilamentController.java
│   │   └── PrintingConfigController.java
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   ├── FilamentNotFoundException.java
│   │   ├── FilamentDuplicateException.java
│   │   └── PrintingConfigNotFoundException.java
│   ├── model/
│   │   ├── Filament.java
│   │   ├── PrintingConfig.java
│   │   └── FilamentStatus.java
│   ├── repository/
│   │   ├── FilamentRepository.java
│   │   └── PrintingConfigRepository.java
│   ├── security/
│   │   └── SecurityConfig.java
│   └── service/
│       ├── FilamentService.java
│       └── PrintingConfigService.java
├── src/main/resources/
│   ├── application.yml
│   └── database/
│       └── init.sql
├── src/test/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
├── pom.xml
└── README.md
```

## Notas Importantes

- Este microservicio es propietario de `config_db` y administra exclusivamente la configuración de costos
- No calcula precios de productos ni modifica productos
- `ms-products` consume su API cuando necesita datos de configuración
- No se realizan DELETE físicos como operación administrativa normal
- Para integración service-to-service, la estrategia de Access Token aún está pendiente de definición por la asignatura

## Licencia

Apache 2.0

## Contacto

Duoc UC - Desarrollo Cloud Native 1
