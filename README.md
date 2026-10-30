# VetAndes API

Examen Parcial II, versión B. Spring Boot 4.1.2, Java 21, Maven, Oracle, Flyway, Spring Security, JWT y Swagger.

## Ejecución

Se necesita Oracle activo y un esquema vacío. La conexión predeterminada usa `localhost:1522/freepdb1`, siguiendo tarea05. En SQL Developer, conectado como administrador al PDB correspondiente:

```sql
CREATE USER VETANDES IDENTIFIED BY "Vet2026";
GRANT CREATE SESSION, CREATE TABLE, CREATE SEQUENCE TO VETANDES;
ALTER USER VETANDES QUOTA UNLIMITED ON USERS;
```

Desde esta carpeta:

```powershell
.\mvnw.cmd spring-boot:run
```

La migración `src/main/resources/db/migration/V1__esquema_y_semilla.sql` crea el esquema y la semilla. No ejecutarla manualmente ni editarla una vez aplicada. Al iniciar se cifran con BCrypt las contraseñas semilla vacías.

Para cambiar la conexión, definir antes de ejecutar:

```powershell
$env:DB_URL='jdbc:oracle:thin:@//localhost:1522/freepdb1'
$env:DB_USERNAME='VETANDES'
$env:DB_PASSWORD='Vet2026'
```

`JWT_SECRET` sustituye la clave Base64 de desarrollo. Tokens de 3600 segundos. API: `http://localhost:8080/api/v1`. Swagger: `http://localhost:8080/swagger-ui.html`. CORS permite `http://localhost:4200`.

## Usuarios de prueba

| Usuario | Contraseña | Rol | Estado |
|---|---|---|---|
| admin | Vet2026* | ADMIN | Activo |
| recepcion | Vet2026* | RECEPCIONISTA | Activo |
| veterinario | Vet2026* | VETERINARIO | Activo |
| inactivo | Vet2026* | RECEPCIONISTA | Inactivo |

Los permisos de varios roles se acumulan. La verificación usa `hasAuthority` y `@PreAuthorize`; los controladores no comparan nombres de rol.

## Contrato del API

Todas las rutas tienen el prefijo `/api/v1`. Excepto el login, necesitan `Authorization: Bearer <token>`.

| Método | Ruta | Permiso |
|---|---|---|
| POST | /auth/login | Público |
| GET | /auth/me | Autenticado |
| GET | /propietarios | MASCOTA_LEER |
| GET | /prestaciones?especie=PERRO | PRESTACION_LEER |
| GET | /prestaciones/{id} | PRESTACION_LEER |
| POST | /prestaciones | PRESTACION_ESCRIBIR |
| PUT, DELETE | /prestaciones/{id} | PRESTACION_ESCRIBIR |
| PATCH | /prestaciones/{id}/estado | PRESTACION_ESCRIBIR |
| GET | /mascotas?propietarioId=1 | MASCOTA_LEER |
| GET | /mascotas/{id} | MASCOTA_LEER |
| POST | /mascotas | MASCOTA_ESCRIBIR |
| PUT, DELETE | /mascotas/{id} | MASCOTA_ESCRIBIR |
| POST | /atenciones | ATENCION_REGISTRAR |
| GET | /atenciones?mascota=Lun&especie=PERRO&estado=REGISTRADA&desde=2026-10-01&hasta=2026-10-30 | ATENCION_LEER |
| GET | /atenciones/{id} | ATENCION_LEER |
| PATCH | /atenciones/{id}/anular | ATENCION_ANULAR |
| GET | /reportes/atenciones-por-especie?desde=2026-10-01&hasta=2026-10-30 | REPORTE_VER |

Los filtros son opcionales y combinables. Las fechas incluyen ambos días. La consulta de atenciones busca nombre parcial sin distinguir mayúsculas, ordena por fecha descendente y devuelve `[]` sin resultados. El reporte devuelve `especie`, `cantidadAtenciones` e `importeTotal`, excluye anuladas y ordena por importe descendente.

Cuerpos de petición:

```json
{
  "login": {"username":"recepcion","password":"Vet2026*"},
  "prestacion": {"codigo":"PRS-010","nombre":"Control general","precio":45,"especieAplicable":"TODAS"},
  "estado": {"activo":false},
  "mascota": {"nombre":"Kira","especie":"PERRO","raza":"Mestizo","fechaNacimiento":"2023-01-01","sexo":"H","propietarioId":1},
  "atencion": {"mascotaId":1,"peso":12.5,"diagnostico":"Control general","detalles":[{"prestacionId":1,"cantidad":1},{"prestacionId":2,"cantidad":1},{"prestacionId":4,"cantidad":1}]},
  "anulacion": {"motivo":"Registro duplicado por error"}
}
```

Los nombres exteriores del ejemplo agrupan operaciones; se envía únicamente el objeto de la operación correspondiente.

El login devuelve `token`, `tipo`, `expiraEnSegundos`, `username`, `nombreCompleto`, `roles` y `permisos`. `/auth/me` devuelve datos, roles y permisos sin contraseña. Mascota añade `id` y `propietarioNombre`, sin anidar entidades. Una atención devuelve cabecera, detalles y totales calculados en el servicio.

201 al registrar, 200 al consultar o actualizar y 204 al eliminar. Errores: 400 validación/formato; 401 token ausente, alterado o vencido, credenciales incorrectas o cuenta inactiva; 403 autenticado sin permiso; 404 recurso inexistente; 409 conflicto de negocio; 500 inesperado.

```json
{"timestamp":"2026-10-30T11:00:00","status":400,"error":"Bad Request","mensaje":"Datos inválidos","ruta":"/api/v1/mascotas","campos":{"fechaNacimiento":"La fecha de nacimiento no puede ser futura"}}
```

`campos` aparece únicamente en errores de validación.

## Reglas

Código de prestación único; una prestación usada no se elimina y se puede dar de baja mediante PATCH. Mascota única por nombre y propietario; no se elimina si tiene atenciones. Atención con uno a ocho detalles distintos, cantidades de uno a tres, peso positivo y prestaciones activas compatibles con la especie. El servicio obtiene los precios, calcula los importes e ignora los enviados por el cliente. Tres prestaciones distintas aplican 10 % de descuento: la atención del ejemplo suma 160, descuenta 16 y totaliza 144. Cabecera y detalles se guardan con `@Transactional`. Una secuencia genera `AT-000001`. Solo se anulan atenciones REGISTRADAS dentro de 48 horas y con motivo de 10 a 200 caracteres.

## Pruebas y entrega

```powershell
.\mvnw.cmd test
.\mvnw.cmd package
```

19 pruebas de integración con H2 en modo Oracle y la misma migración de Flyway. Verifican CRUD, JWT, BCrypt, permisos acumulados, errores, descuento, especie, rechazo sin registros parciales, anulación, filtros, reporte, Swagger y CORS. H2 es únicamente dependencia de pruebas.

La colección `postman/VetAndes.postman_collection.json` incluye ejemplos reales de 200/201, 400, 401, 403, 404 y 409. Ejecutar primero los login para guardar los tokens. Los ejemplos y capturas se obtuvieron con el perfil H2 de pruebas, consumiendo la API real. Se verificaron el arranque con Oracle, la validación del esquema, el login y las consultas de prestaciones, mascotas, atenciones y reporte. Las capturas y los ejemplos conservan su entorno H2 de pruebas.

Ramas locales `main`, `develop` y `examen-b-condori`, etiqueta `v1.0-examen-u2`. Falta configurar y publicar el remoto propio `vetandes-backend`.
