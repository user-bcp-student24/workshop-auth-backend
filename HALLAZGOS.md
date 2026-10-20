# Hallazgos

Evidencia basada en el código actual. Los hallazgos SCA registran las versiones
fijadas en el proyecto; este inventario no confirma por sí solo advisories o
CVEs concretos.

| id | hallazgo | tipo | explicacion | evidencia (archivo:linea) | severidad | estado |
|---|---|---|---|---|---|---|
| H-01 | Concatenación de credenciales en SQL | Vulnerabilidad | Los valores recibidos de usuario y contraseña se interpolan en la consulta y esta se ejecuta como SQL. Entradas con contenido SQL pueden alterar la condición de autenticación y permitir acceso no autorizado. | `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:34-42` | alta | Resuelto: consulta parametrizada con `PreparedStatement` y parámetros JDBC. |
| H-02 | Contraseñas en texto plano | Vulnerabilidad | La tabla almacena el campo `password` directamente y las cuentas iniciales se insertan con contraseñas literales. Una lectura o filtración de la base expone credenciales reutilizables sin una barrera de hash lento con salt. | `src/main/java/com/bcp/workshop/auth/LegacyDatabase.java:22-24` | alta | Pendiente |
| H-03 | Token Base64 con secreto embebido, sin firma | Vulnerabilidad | El token legado se sustituyó por JWT HS256; el backend emite y Requirements API verifica firma, emisor, audiencia y expiración usando configuración externa. | `src/main/java/com/bcp/workshop/auth/AuthTokenService.java:22,29-35`; `../workshop-requirements-api/app/main.py:17-35` | alta | Resuelto: pruebas Java y Python verifican contrato, firma, claims y expiración. |
| H-04 | Secreto y política CORS hardcodeados | Vulnerabilidad | El secreto de firma del token se externalizó mediante `AUTH_TOKEN_SECRET`; CORS todavía permite cualquier origen mediante `/.*/`. | `src/main/resources/application.properties:5,9` | alta | Parcial: secreto del token externalizado; CORS sigue pendiente. |
| H-05 | Respuesta de error expone detalles internos | Vulnerabilidad | Ante una excepción, la respuesta incluye el mensaje original y el nombre de la clase de excepción. Estos datos pueden revelar detalles de implementación o infraestructura a quien invoque el endpoint. | `src/main/java/com/bcp/workshop/auth/AuthResource.java:54-58` | media | Pendiente |
| H-06 | Dependencias señaladas para revisión SCA | Sugerencia | El BOM de Quarkus está fijado en `3.27.4` y Commons Text en `1.9`. Las versiones constan en el manifiesto, pero su presencia no demuestra por sí sola que exista un CVE aplicable; hay que contrastarlas con advisories y el árbol de dependencias. | `pom.xml:13,49-50` | media | Pendiente |
| H-07 | `AuthResource` concentra validación, negocio, persistencia y respuesta | Refactor | El método de login valida la petición, consulta la base, crea el token, compone la respuesta y maneja excepciones en el mismo flujo. Esto dificulta probar y modificar las responsabilidades de forma aislada. | `src/main/java/com/bcp/workshop/auth/AuthResource.java:24-58` | baja | Pendiente |
