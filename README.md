# MS-Config

Microservicio de configuración de costos para PrintWorks. Este servicio es responsable de administrar la configuración utilizada para el cálculo de precios del sistema, incluyendo filamentos, precios de electricidad y consumo energético de impresoras.

Además, se integra con `ms-products` para notificar cambios de configuración que puedan afectar los precios calculados de los productos.

## Tecnologías

- Java 21
- Spring Boot 3.4.11
- Maven
- Spring Data JPA
- Spring Security con OAuth2 Resource Server
- Spring Web / RestTemplate
- MySQL 8.0
- Lombok
- SpringDoc OpenAPI (Swagger)
- Jakarta Validation
- JUnit 5
- Docker

## Características

- Gestión de filamentos:
  - Crear filamentos
  - Listar filamentos
  - Actualizar filamentos
  - Activar o desactivar filamentos
- Gestión de configuración de impresión:
  - Precio de electricidad por kWh
  - Consumo energético de la impresora
- Detección de cambios de costos que afectan los precios de productos
- Integración HTTP con `ms-products`
- Invalidación de productos asociados cuando cambia el precio de un filamento
- Invalidación global de productos cuando cambia la configuración energética
- Propagación del Access Token JWT en comunicaciones hacia `ms-products`
- API REST versionada bajo `/api/v1/config`
- Autenticación mediante OAuth2 Resource Server (JWT Bearer Token)
- Documentación API con Swagger/OpenAPI
- Validación de datos con Jakarta Validation
- Manejo centralizado de excepciones
- Soporte para CORS
- Tests unitarios y de integración
- Contenedor Docker con docker-compose

## Arquitectura

El microservicio mantiene una arquitectura por capas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Model
```

Para la comunicación con otros microservicios se utiliza adicionalmente una capa cliente:

```text
Controller
    ↓
Service
    ├── Repository → config_db
    │
    └── ProductServiceClient
              ↓
         ms-products
```

Las entidades JPA nunca se exponen directamente; se utilizan DTOs para las solicitudes y respuestas.

`ms-config` es propietario exclusivamente de `config_db`. La modificación de productos continúa siendo responsabilidad de `ms-products`.

## Integración con MS-Products

Los precios de los productos dependen de valores administrados por este microservicio.

Por este motivo, cuando una configuración que participa en el cálculo de precios cambia, `ms-config` notifica a `ms-products`.

### Cambio de precio de filamento

Cuando cambia `pricePerKg` de un filamento:

```text
Actualización de filamento
        ↓
ms-config detecta cambio de pricePerKg
        ↓
ProductServiceClient
        ↓
POST ms-products
/api/v1/products/internal/invalidate/filament/{idFilament}
        ↓
Productos asociados al filamento
        ↓
INACTIVE + OUTDATED
```

Los cambios de nombre o color del filamento no provocan invalidación de precios.

### Cambio de configuración de impresión

Cuando cambia alguno de los siguientes valores:

- Precio de electricidad por kWh
- Consumo energético de la impresora

se notifica a `ms-products`:

```text
Actualización configuración de impresión
        ↓
ms-config detecta cambio de costos
        ↓
ProductServiceClient
        ↓
POST ms-products
/api/v1/products/internal/invalidate/printing
        ↓
Todos los productos
        ↓
INACTIVE + OUTDATED
```

La invalidación permite evitar que permanezcan publicados productos cuyo precio fue calculado utilizando costos anteriores.

El recálculo y la posterior activación de los productos son responsabilidad de `ms-products` y del flujo administrativo correspondiente.

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

Todos los endpoints de configuración requieren autenticación mediante JWT Bearer Token.

## Configuración

### Variables de Entorno

El servicio utiliza las siguientes variables de entorno:

- `SERVER_PORT` - Puerto del servidor. Default: `8080`
- `DB_HOST` - Host de base de datos MySQL. Default: `localhost`
- `DB_PORT` - Puerto de base de datos MySQL. Default: `3306`
- `DB_USER` - Usuario de base de datos. Default: `root`
- `DB_PASSWORD` - Contraseña de base de datos
- `MS_PRODUCTS_BASE_URL` - URL base utilizada para comunicarse con `ms-products`. Default local: `http://localhost:8081`

Ejemplo local:

```env
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=3306
DB_USER=root
DB_PASSWORD=
MS_PRODUCTS_BASE_URL=http://localhost:8081
```

La configuración de `ms-products` se encuentra definida en `application.yml`:

```yaml
services:
  products:
    base-url: ${MS_PRODUCTS_BASE_URL:http://localhost:8081}
```

## Seguridad

El servicio funciona como OAuth2 Resource Server y valida Access Tokens JWT emitidos por Microsoft Entra ID.

Configuración actual:

```yaml
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://login.microsoftonline.com/14c46308-ab91-4693-b0c0-daddd7a8aab5/v2.0
          audiences:
            - api://80bf85e9-a444-4754-b780-c65dfff74876
            - 80bf85e9-a444-4754-b780-c65dfff74876
```

Los endpoints protegidos requieren:

```http
Authorization: Bearer <access_token>
```

El ID Token no se utiliza como sustituto del Access Token.

### Comunicación entre microservicios

Cuando una solicitud autenticada provoca una notificación hacia `ms-products`, `ProductServiceClient` obtiene el Access Token validado desde el contexto de seguridad y lo propaga:

```text
front-admin
    ↓ Access Token
ms-config
    ↓ mismo Access Token
ms-products
```

De esta forma, `ms-products` puede validar la solicitud utilizando su configuración de OAuth2 Resource Server.

## Base de Datos

`ms-config` es propietario de la base de datos:

```text
config_db
```

El esquema se inicializa utilizando:

```text
src/main/resources/database/init.sql
```

### Tablas

- `filaments` - Información y costos de filamentos de impresión 3D
- `printing_config` - Configuración de costos energéticos de impresión

Los demás microservicios no acceden directamente a `config_db`. Cuando necesitan esta información deben consumir la API de `ms-config`.

## Instalación y Ejecución

### Requisitos Previos

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Docker (opcional)

### Ejecución Local

Para desarrollo local se utiliza por defecto:

```text
ms-config    → http://localhost:8080
ms-products  → http://localhost:8081
```

1. Clonar el repositorio.
2. Configurar MySQL y las variables de entorno necesarias.
3. Asegurarse de que `config_db` esté disponible.
4. Ejecutar:

```bash
mvn spring-boot:run
```

El servicio quedará disponible en:

```text
http://localhost:8080
```

Para probar las funcionalidades de invalidación de precios también debe estar ejecutándose `ms-products` en el puerto configurado mediante `MS_PRODUCTS_BASE_URL`.

### Ejecución con Docker

Construir la imagen:

```bash
docker build -t ms-config .
```

Ejecutar con docker-compose:

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

La documentación Swagger está disponible localmente en:

```text
http://localhost:8080/swagger-ui.html
```

La especificación OpenAPI está disponible en:

```text
http://localhost:8080/api-docs
```

## Tests

El proyecto contempla:

- Tests unitarios de servicios
- Mockito
- Tests de seguridad de API
- Spring Security Test
- Tests de validación de DTOs
- Tests de integración

Los cambios relacionados con invalidación de precios deben considerar especialmente los siguientes escenarios:

- Cambiar el precio de un filamento notifica a `ms-products`
- Cambiar solamente nombre o color no invalida precios
- Guardar el mismo precio no provoca invalidación
- Cambiar el precio de electricidad invalida productos
- Cambiar el consumo energético invalida productos
- Guardar los mismos valores de impresión no provoca invalidación

## Estructura del Proyecto

```text
ms-config/
├── database/
│   └── init.sql
├── src/main/java/duoc/cn1/ms_config/
│   ├── MsConfigApplication.java
│   │
│   ├── client/
│   │   └── ProductServiceClient.java
│   │
│   ├── config/
│   │   ├── OpenApiConfig.java
│   │   └── RestTemplateConfig.java
│   │
│   ├── controller/
│   │   ├── FilamentController.java
│   │   └── PrintingConfigController.java
│   │
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   ├── FilamentNotFoundException.java
│   │   ├── FilamentDuplicateException.java
│   │   └── PrintingConfigNotFoundException.java
│   │
│   ├── model/
│   │   ├── Filament.java
│   │   ├── PrintingConfig.java
│   │   └── FilamentStatus.java
│   │
│   ├── repository/
│   │   ├── FilamentRepository.java
│   │   └── PrintingConfigRepository.java
│   │
│   ├── security/
│   │   └── SecurityConfig.java
│   │
│   └── service/
│       ├── FilamentService.java
│       └── PrintingConfigService.java
│
├── src/main/resources/
│   ├── application.yml
│   └── database/
│       └── init.sql
│
├── src/test/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
├── pom.xml
└── README.md
```

## Responsabilidades del Microservicio

`ms-config` es responsable de:

- Administrar filamentos
- Administrar costos energéticos
- Mantener `config_db`
- Exponer la configuración mediante API REST
- Detectar cambios de costos que afectan precios
- Notificar a `ms-products` cuando esos costos cambian

`ms-config` **no es responsable de**:

- Calcular el precio final de productos
- Almacenar productos
- Modificar directamente `products_db`
- Recalcular productos
- Publicar o activar productos
- Gestionar pedidos

Estas responsabilidades pertenecen a los microservicios correspondientes.

## Flujo General con MS-Products

```text
                   ┌───────────────────┐
                   │     ms-config     │
                   │     config_db     │
                   └─────────┬─────────┘
                             │
                 cambio de configuración
                             │
                             ▼
                  ProductServiceClient
                             │
                             ▼
                   ┌───────────────────┐
                   │    ms-products    │
                   │    products_db    │
                   └─────────┬─────────┘
                             │
                             ▼
                    INACTIVE + OUTDATED
                             │
                      administrador
                         recalcula
                             │
                             ▼
                    INACTIVE + CURRENT
                             │
                      administrador
                          activa
                             │
                             ▼
                     ACTIVE + CURRENT
```

## Notas Importantes

- Este microservicio es propietario exclusivamente de `config_db`.
- `ms-products` no accede directamente a las tablas de `ms-config`.
- `ms-products` consume la API de configuración para calcular precios.
- `ms-config` notifica a `ms-products` cuando detecta cambios de costos que afectan precios.
- La comunicación entre servicios se realiza mediante HTTP REST.
- Actualmente se propaga el Access Token de la solicitud administrativa hacia `ms-products`.
- Los productos afectados se invalidan como `OUTDATED` e `INACTIVE`.
- El recálculo de precios no es responsabilidad de `ms-config`.
- La reactivación de productos después de revisar un precio recalculado es manual.
- No se realizan DELETE físicos como operación administrativa normal.

## Licencia

Apache 2.0

## Contacto

Duoc UC - Desarrollo Cloud Native I