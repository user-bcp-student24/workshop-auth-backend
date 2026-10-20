# Plan de corrección

## Aprobación

Aprobado por la persona solicitante mediante confirmación explícita (“go”) en
la conversación del 20 de octubre de 2026.

## Selección

- **H-01**, priorizado por la persona solicitante por su riesgo alto: los valores
  de credenciales concatenados en SQL pueden alterar la consulta de autenticación.
- Al momento de seleccionar este trabajo, el hallazgo figuraba como
  **Pendiente** en `HALLAZGOS.md`; su estado actual se registra allí.

## H-01 — Concatenación de credenciales en SQL

### Problema

`LegacyDatabase.findByCredentials` inserta `username` y `password`, recibidos
desde el flujo de login, directamente en una cadena SQL que ejecuta mediante
`Statement`. La consulta no separa los datos de la estructura SQL, por lo que
un valor de entrada podría modificar la condición de búsqueda y permitir una
autenticación indebida. La evidencia registrada es
`src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:32-35`.

### Cambio propuesto

Reemplazar la consulta construida por concatenación por una consulta con dos
parámetros (`username` y `password`) y ejecutarla con `PreparedStatement`.
Asignar ambos valores mediante los métodos de parámetros JDBC; mantener la
conexión y los recursos en `try-with-resources`.

Limitar el cambio de producción a la consulta de autenticación. No cambiar la
ruta, el método HTTP, el formato JSON, los estados ni los mensajes definidos en
`docs/ENDPOINTS.md`. Mantener `findByCredentials` con una única responsabilidad:
buscar por las credenciales indicadas y mapear el registro encontrado a
`UserRecord`. No agregar dependencias.

### Archivos a tocar

- `src/test/java/com/bcp/workshop/auth/AuthResourceTest.java`: añadir primero
  las pruebas de caracterización descritas abajo.
- `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java`: parametrizar la
  consulta y sus dos credenciales con JDBC.

### Pruebas de caracterización antes del cambio de producción

Escribir y ejecutar las pruebas HTTP antes de modificar `LegacyDatabase.java`:

1. **Login válido:** enviar el usuario local `student` con su contraseña
   configurada y confirmar `200`, `token` presente y los mismos campos de
   usuario (`username`, `displayName`, `role`) que define el contrato.
2. **Credenciales no coincidentes:** confirmar que un usuario/contraseña
   ordinarios que no existen devuelven `401` y conservan el cuerpo de error
   especificado en `docs/ENDPOINTS.md`.
3. **Entradas que contienen caracteres con significado en SQL:** con un nombre
   y una contraseña que no correspondan a una cuenta real, confirmar que el
   login devuelve `401` y no autentica. La prueba debe validar el resultado
   observable sin depender de detalles de implementación de JDBC.

Después de parametrizar la consulta, volver a ejecutar estas pruebas y la suite
pertinente. Confirmar que los casos válidos y los estados/respuestas observados
siguen respetando el contrato de `POST /auth/login`.
