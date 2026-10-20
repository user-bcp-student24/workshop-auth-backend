# Contrato de endpoints (implementación actual)

Este documento describe el comportamiento observado en el código. En particular,
el contrato de `POST /auth/login` debe preservarse al corregir internamente el
flujo de autenticación.

## `POST /auth/login`

- **Content-Type:** `application/json`
- **Accept:** `application/json`
- **Cuerpo:**

  ```json
  {
    "username": "student",
    "password": "Workshop123!",
    "client": "angular-workshop"
  }
  ```

  `username` y `password` son obligatorios y no pueden ser nulos ni estar en
  blanco. `client` es opcional y actualmente no participa en la autenticación.
  La configuración Jackson permite propiedades JSON no reconocidas.

- **Flujo actual:** valida los campos, busca las credenciales en la base SQLite,
  genera el token para el usuario encontrado y devuelve los datos públicos del
  usuario junto con la duración y un campo de depuración.

- **Respuesta `200 OK`:**

  ```json
  {
    "token": "<token>",
    "expiresIn": 3600,
    "user": {
      "username": "student",
      "displayName": "Workshop Student",
      "role": "STUDENT"
    },
    "debug": "authenticated-with-legacy-flow"
  }
  ```

- **Errores actuales:**
  - `400 Bad Request`: cuerpo nulo, `username` nulo/en blanco o `password`
    nula/en blanco. El cuerpo contiene `{"message":"..."}`.
  - `401 Unauthorized`: credenciales sin coincidencia. El cuerpo contiene
    `{"message":"Invalid user <username> or password"}`.
  - `500 Internal Server Error`: excepción durante el flujo. El cuerpo contiene
    `message` y `exception`.

## `GET /q/health`

Endpoint de salud de Quarkus expuesto por la extensión SmallRye Health, según
la configuración declarada por la aplicación.

## Persistencia usada por el login

La aplicación usa SQLite mediante JDBC. La URL se obtiene de `AUTH_DB_URL` o,
si no está definida, usa `jdbc:sqlite:./data/workshop.db`. Al inicializarse,
crea la tabla `legacy_users` e inserta las cuentas locales `student` y
`trainer` si aún no existen.
