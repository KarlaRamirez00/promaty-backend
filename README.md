# Promaty — Backend

Backend de Promaty: sistema de gestión de solicitudes para el ciclo de vida del colaborador,
desarrollado para una empresa del rubro construcción, como Proyecto de Título (CAPSTONE).

Descripción completa del proyecto, arquitectura e integrantes en el repositorio de evidencias
académicas: [CAPSTONE_003V](https://github.com/KarlaRamirez00/CAPSTONE_003V).

## Tecnologías

- Java + Spring Boot
- Spring Cloud (Eureka, OpenFeign)
- PostgreSQL

## Arquitectura — microservicios

| Servicio | Puerto | Responsabilidad | Depende de |
|---|---|---|---|
| `eureka-server` | `EUREKA_PORT` | Service discovery — registro de todos los demás servicios | — |
| `user-server` | 8081 | Usuarios, Roles, Permisos, Módulos/Submódulos (RBAC). Dueño exclusivo de su base de datos | Eureka |
| `authorizer-server` | 8082 | Login, emisión y validación de JWT. Sin base de datos propia — consulta a `user-server` vía OpenFeign | Eureka, user-server |
| `gateway-server` | 8083 | Punto de entrada único: enruta hacia los demás servicios y valida el JWT localmente | Eureka, user-server, authorizer-server, rrhh-server |
| `rrhh-server` | 8085 | Dominio de negocio de RRHH (gestión de solicitudes del ciclo de vida del colaborador). Dueño exclusivo de su base de datos | Eureka, user-server (permisos) |

El frontend consume el sistema a través de `gateway-server`, no directamente contra cada servicio.

## Requisitos previos

- JDK y Maven
- PostgreSQL corriendo localmente, con las bases de datos de cada servicio con base de datos propia
  (`promaty_user` para `user-server`, `promaty_rrhh` para `rrhh-server`)

## Configuración

Cada servicio lee credenciales y parámetros desde variables de entorno / un archivo de configuración
local no versionado (`application-local.yml`, ver `.gitignore`). Como mínimo se requiere definir:

- `DB_USERNAME` / `DB_PASSWORD` (en los servicios con base de datos propia)
- `EUREKA_PORT` (en todos los servicios, para registrarse en Eureka)

## Orden de levantamiento

```
1. eureka-server
2. user-server
3. authorizer-server
4. rrhh-server
5. gateway-server
```

## Ejecución local

Desde cada módulo:

```bash
./mvnw spring-boot:run
```

## Integrante

| Nombre | Rol |
|---|---|
| Karla Ramírez Hidalgo | Desarrollo full stack, análisis y gestión del proyecto (trabajo individual) |

## Metodología de trabajo

Kanban: backlog priorizado con jerarquía Épica → Historia de Usuario → Tarea, tablero con estados
(Por Hacer / En desarrollo / Terminado), sin sprints de duración fija.
