# Documentación — Módulo Cliente (DunkelWeiss)

## Índice
1. [Visión general](#1-visión-general)
2. [Estructura de archivos](#2-estructura-de-archivos)
3. [Patrones de diseño aplicados](#3-patrones-de-diseño-aplicados)
4. [Capa de configuración](#4-capa-de-configuración)
5. [Capa de servicio](#5-capa-de-servicio)
6. [Capa de controlador](#6-capa-de-controlador)
7. [Vistas Thymeleaf](#7-vistas-thymeleaf)
8. [DTO compartido (lib)](#8-dto-compartido-lib)
9. [API del servidor sugerida](#9-api-del-servidor-sugerida)
10. [Flujo completo de usuario](#10-flujo-completo-de-usuario)

---

## 1. Visión general

El módulo cliente es una aplicación Spring Boot independiente que corre en el **puerto 8081** y se comunica con el servidor REST en el **puerto 8080**. Utiliza **Thymeleaf** como motor de plantillas para renderizar HTML en el servidor.

La primera pantalla que ve el usuario es un formulario de autenticación con dos modos: **Sign In** (inicio de sesión) y **Sign Up** (registro), alternables desde un botón en la esquina superior derecha. Tras un login exitoso se muestra una página de inicio con el nombre del proyecto.

---

## 2. Estructura de archivos

```
client/
├── src/main/java/client/
│   ├── App.java                         # Punto de entrada Spring Boot
│   ├── config/
│   │   └── AppConfig.java               # Bean RestTemplate + URL del servidor
│   ├── controller/
│   │   └── AuthController.java          # Rutas HTTP de autenticación y home
│   └── service/
│       └── AuthService.java             # Lógica de llamadas al servidor
│
├── src/main/resources/
│   ├── application.properties           # Puerto 8081 + URL del servidor
│   ├── templates/
│   │   ├── auth.html                    # Página de login/registro (Thymeleaf)
│   │   └── home.html                    # Página principal tras login
│   └── static/
│       └── css/
│           └── auth.css                 # Estilos del formulario de autenticación
│
└── build.gradle.kts                     # Dependencias: WebMVC, Thymeleaf, lib

lib/
└── src/main/java/lib/dto/
    └── UserCredentialsDto.java          # DTO compartido (username + password)
```

---

## 3. Patrones de diseño aplicados

### MVC (Model-View-Controller)
- **Model**: `UserCredentialsDto` transporta los datos entre capas.
- **View**: plantillas Thymeleaf (`auth.html`, `home.html`) renderizan el HTML.
- **Controller**: `AuthController` recibe peticiones HTTP, delega en el servicio y elige la vista a devolver.

```
Petición HTTP → AuthController → AuthService → Servidor REST
                     ↓
               Thymeleaf Template → HTML al navegador
```

### Service Layer (Capa de servicio)
`AuthService` encapsula toda la lógica de comunicación con el servidor. El controlador no sabe nada de HTTP ni de URLs; simplemente llama a `login()` o `register()` y recibe un `boolean`.

### Facade (Fachada)
`AppConfig` configura y expone un único bean `RestTemplate` para toda la aplicación. El resto de clases lo inyectan sin preocuparse de cómo está construido ni configurado.

---

## 4. Capa de configuración

### `AppConfig.java`
**Ruta:** `client/src/main/java/client/config/AppConfig.java`

| Elemento | Descripción |
|----------|-------------|
| `@Configuration` | Marca la clase como fuente de beans de Spring |
| `@Value("${server.api.url}")` | Inyecta la URL del servidor desde `application.properties` |
| `restTemplate()` | Crea el bean HTTP reutilizable en toda la aplicación |
| `getServerApiUrl()` | Permite que los servicios accedan a la URL base |

### `application.properties`
```properties
server.port=8081
server.api.url=http://localhost:8080
```

---

## 5. Capa de servicio

### `AuthService.java`
**Ruta:** `client/src/main/java/client/service/AuthService.java`

#### `login(UserCredentialsDto credentials) → boolean`
- Hace `POST` a `/api/auth/login` con el DTO serializado como JSON.
- Devuelve `true` si el servidor responde `200 OK`.
- Devuelve `false` si recibe `401 Unauthorized` o cualquier error de red.

#### `register(UserCredentialsDto credentials) → boolean`
- Hace `POST` a `/api/auth/register` con el DTO serializado como JSON.
- Devuelve `true` si el servidor responde `201 Created`.
- Devuelve `false` si recibe `409 Conflict` (usuario ya existe) o cualquier error de red.

> Los errores `HttpClientErrorException` (4xx) y los errores de red se capturan internamente; nunca lanzan excepción al controlador.

---

## 6. Capa de controlador

### `AuthController.java`
**Ruta:** `client/src/main/java/client/controller/AuthController.java`

| Método HTTP | Ruta | Descripción |
|-------------|------|-------------|
| `GET` | `/` | Muestra `auth.html`. Acepta `?mode=signin\|signup` y `?error=true` |
| `POST` | `/login` | Procesa el formulario de login. Redirige a `/home` o `/?error=true&mode=signin` |
| `POST` | `/register` | Procesa el formulario de registro. Redirige a `/` o `/?error=true&mode=signup` |
| `GET` | `/home` | Muestra `home.html` |

**Parámetros de URL usados:**
- `mode=signin` / `mode=signup` — indica al JS qué formulario mostrar al cargar.
- `error=true` — indica a Thymeleaf que muestre el mensaje de error.

---

## 7. Vistas Thymeleaf

### `auth.html`
**Ruta:** `client/src/main/resources/templates/auth.html`

Página única con dos secciones (`#signin-section` y `#signup-section`). Solo una está visible a la vez.

**Elementos clave:**
- **Botón toggle** (esquina superior derecha): llama a `toggleMode()` en JS para alternar entre los formularios sin recargar la página.
- **Mensaje de error**: bloque `th:if="${error}"` que solo se renderiza si el controlador añadió `error=true` al modelo.
- **Formulario Sign In**: `POST /login` con campos `username` y `password`.
- **Formulario Sign Up**: `POST /register` con campos `username` y `password`.

**Lógica JavaScript (`auth.html`):**

| Función | Qué hace |
|---------|----------|
| `toggleMode()` | Detecta qué sección está visible y llama a `showSignin()` o `showSignup()` |
| `showSignin()` | Muestra `#signin-section`, oculta `#signup-section`, cambia el texto del botón a "Sign Up" |
| `showSignup()` | Muestra `#signup-section`, oculta `#signin-section`, cambia el texto del botón a "Sign In" |
| Inicialización | Lee `?mode=` de la URL al cargar; si es `signup`, arranca en modo registro (útil tras un error de registro) |

### `home.html`
**Ruta:** `client/src/main/resources/templates/home.html`

Página mínima que muestra el nombre del proyecto centrado en pantalla. Punto de llegada tras un login exitoso.

### `auth.css`
**Ruta:** `client/src/main/resources/static/css/auth.css`

Estilos para el layout del formulario: cabecera con toggle, tarjeta centrada, campos de formulario, botón de envío y mensaje de error en rojo.

---

## 8. DTO compartido (lib)

### `UserCredentialsDto.java`
**Ruta:** `lib/src/main/java/lib/dto/UserCredentialsDto.java`

Clase compartida entre cliente y servidor a través del módulo `lib`.

| Campo | Tipo | Validación | Anotación Jackson |
|-------|------|-----------|-------------------|
| `username` | `String` | `@NotBlank` | `@JsonProperty("username")` |
| `password` | `String` | `@NotBlank` | `@JsonProperty("password")` |

Incluye constructor vacío (requerido por Jackson para deserialización), constructor con parámetros, y getters/setters.

---

## 9. API del servidor sugerida

Endpoints que el servidor debe implementar para que el cliente funcione:

```
POST /api/auth/login
  Content-Type: application/json
  Body: { "username": "string", "password": "string" }

  Respuestas:
    200 OK              → Credenciales válidas
    401 Unauthorized    → Credenciales incorrectas
```

```
POST /api/auth/register
  Content-Type: application/json
  Body: { "username": "string", "password": "string" }

  Respuestas:
    201 Created         → Usuario registrado correctamente
    409 Conflict        → El nombre de usuario ya existe
```

> Mientras el servidor no esté disponible, el cliente captura los errores de red y muestra el mensaje de error en el formulario sin romper la aplicación.

---

## 10. Flujo completo de usuario

```
Usuario abre http://localhost:8081/
        │
        ▼
GET /  →  auth.html  (modo Sign In por defecto)
        │
        ├─ [Pulsa toggle "Sign Up"]  ──►  JS muestra formulario Sign Up
        │
        ├─ [Rellena Sign Up y envía]
        │       │
        │       ▼
        │   POST /register  →  AuthService.register()
        │       │                     │
        │       │                POST :8080/api/auth/register
        │       │                     │
        │       ├─ 201 Created  ──►  redirect /  (vuelve a Sign In)
        │       └─ fallo       ──►  redirect /?error=true&mode=signup
        │
        └─ [Rellena Sign In y envía]
                │
                ▼
            POST /login  →  AuthService.login()
                │                   │
                │               POST :8080/api/auth/login
                │                   │
                ├─ 200 OK  ──►  redirect /home  →  home.html ("DunkelWeiss")
                └─ fallo   ──►  redirect /?error=true&mode=signin
```
