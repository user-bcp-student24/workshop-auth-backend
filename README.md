# Workshop Auth Backend — Java Quarkus

> **Laboratorio intencionalmente vulnerable.** Ejecutar únicamente en local, repositorios privados y ambientes aislados. No desplegar en Internet, CERT o PROD.

Backend Quarkus que autentica usuarios de laboratorio almacenados en SQLite y entrega un token consumido por el frontend Angular y la API Python.

## Ejecución

Requisitos: JDK 25 (LTS) y Maven 3.9+.

```bash
mvn quarkus:dev
```

Antes de iniciar, configure `AUTH_TOKEN_SECRET` con una clave aleatoria de alta entropía. Requirements API debe usar el mismo valor. No lo guarde en el repositorio.

Servicio: `http://localhost:8080`

Credenciales locales:

- `student` / `Workshop123!`
- `trainer` / `Trainer123!`

## Endpoints

- `POST /auth/login`
- `GET /q/health`

## Propósito de la rama `main`

La rama contiene deuda técnica y de seguridad deliberada: SQL concatenado, contraseñas en texto plano, CORS permisivo, método largo, manejo genérico de excepciones y dependencias con CVEs. La emisión de tokens firmados y la configuración externa de su clave se corrigieron en HU-01.
