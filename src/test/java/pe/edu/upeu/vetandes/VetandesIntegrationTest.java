package pe.edu.upeu.vetandes;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.vetandes.config.JwtService;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VetandesIntegrationTest {
  @LocalServerPort int port;
  @Autowired JdbcTemplate jdbc;
  @Autowired PasswordEncoder encoder;
  @Autowired JwtService jwt;

  @Value("${app.jwt.secret}")
  String secret;

  final ObjectMapper json = new ObjectMapper();
  final HttpClient client = HttpClient.newHttpClient();

  record Respuesta(int status, JsonNode body) {}

  Respuesta request(String method, String path, Object body, String token) throws Exception {
    var builder =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json");
    if (token != null) builder.header("Authorization", "Bearer " + token);
    builder.method(
        method,
        body == null
            ? HttpRequest.BodyPublishers.noBody()
            : HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)));
    var response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    return new Respuesta(
        response.statusCode(), response.body().isBlank() ? null : json.readTree(response.body()));
  }

  String token(String username) throws Exception {
    var r =
        request(
            "POST",
            "/api/v1/auth/login",
            Map.of("username", username, "password", "Vet2026*"),
            null);
    assertEquals(200, r.status(), String.valueOf(r.body()));
    assertEquals(3600, r.body().get("expiraEnSegundos").asInt());
    return r.body().get("token").asText();
  }

  Map<String, Object> atencion(List<Map<String, Integer>> detalles) {
    return new HashMap<>(
        Map.of(
            "mascotaId",
            1,
            "peso",
            12.5,
            "diagnostico",
            "Control de prueba",
            "detalles",
            detalles));
  }

  Map<String, Integer> detalle(int id, int cantidad) {
    return Map.of("prestacionId", id, "cantidad", cantidad);
  }

  Map<String, Object> mascota(String nombre) {
    return new HashMap<>(
        Map.of(
            "nombre",
            nombre,
            "especie",
            "PERRO",
            "raza",
            "Mestizo",
            "fechaNacimiento",
            "2023-01-01",
            "sexo",
            "M",
            "propietarioId",
            5));
  }

  @Test
  void loginProteccionYFormatoComun() throws Exception {
    var r = request("GET", "/api/v1/mascotas", null, null);
    assertEquals(401, r.status());
    for (String campo : List.of("timestamp", "status", "error", "mensaje", "ruta"))
      assertTrue(r.body().has(campo));
    assertFalse(r.body().has("campos"));
    String t = token("admin");
    var me = request("GET", "/api/v1/auth/me", null, t);
    assertEquals(200, me.status());
    assertEquals(8, me.body().get("permisos").size());
    assertFalse(me.body().has("password"));
    assertEquals(
        401,
        request("GET", "/api/v1/mascotas", null, t.substring(0, t.lastIndexOf('.') + 1) + "AAAA")
            .status());
    String vencido =
        new JwtService(secret, -1)
            .generarToken(
                User.withUsername("admin").password("x").authorities("PRESTACION_LEER").build());
    assertEquals(401, request("GET", "/api/v1/mascotas", null, vencido).status());
    assertTrue(
        encoder.matches(
            "Vet2026*",
            jdbc.queryForObject(
                "select password from usuarios where username='admin'", String.class)));
  }

  @ParameterizedTest
  @CsvSource({"noexiste,Vet2026*", "admin,incorrecta", "inactivo,Vet2026*"})
  void loginNoRevelaCuenta(String usuario, String clave) throws Exception {
    var r =
        request("POST", "/api/v1/auth/login", Map.of("username", usuario, "password", clave), null);
    assertEquals(401, r.status());
    assertEquals("Usuario o contraseña incorrectos", r.body().get("mensaje").asText());
  }

  @ParameterizedTest
  @CsvSource({
    "recepcion,GET,/api/v1/prestaciones,200",
    "recepcion,PATCH,/api/v1/atenciones/1/anular,403",
    "recepcion,GET,/api/v1/reportes/atenciones-por-especie,403",
    "recepcion,POST,/api/v1/prestaciones,403",
    "veterinario,POST,/api/v1/mascotas,403",
    "veterinario,POST,/api/v1/atenciones,403",
    "veterinario,GET,/api/v1/reportes/atenciones-por-especie,200"
  })
  void permisosPorOperacion(String usuario, String metodo, String ruta, int esperado)
      throws Exception {
    var r = request(metodo, ruta, metodo.equals("GET") ? null : Map.of(), token(usuario));
    assertEquals(esperado, r.status(), String.valueOf(r.body()));
    if (esperado == 403) {
      assertEquals("Forbidden", r.body().get("error").asText());
      assertFalse(r.body().has("campos"));
    }
  }

  @Test
  void unionDePermisosDeVariosRoles() throws Exception {
    jdbc.update("insert into usuario_roles(usuario_id,rol_id) values(2,3)");
    try {
      var r = request("GET", "/api/v1/auth/me", null, token("recepcion"));
      assertEquals(8, r.body().get("permisos").size());
    } finally {
      jdbc.update("delete from usuario_roles where usuario_id=2 and rol_id=3");
    }
  }

  @Test
  void crudPrestacionesYReglas() throws Exception {
    String t = token("admin");
    var d =
        new HashMap<String, Object>(
            Map.of(
                "codigo",
                "PRS-950",
                "nombre",
                "Consulta de prueba",
                "precio",
                35,
                "especieAplicable",
                "TODAS"));
    var r = request("POST", "/api/v1/prestaciones", d, t);
    assertEquals(201, r.status(), String.valueOf(r.body()));
    long id = r.body().get("id").asLong();
    assertEquals(409, request("POST", "/api/v1/prestaciones", d, t).status());
    d.put("precio", 40);
    assertEquals(200, request("PUT", "/api/v1/prestaciones/" + id, d, t).status());
    assertEquals(
        200,
        request("PATCH", "/api/v1/prestaciones/" + id + "/estado", Map.of("activo", false), t)
            .status());
    assertEquals(204, request("DELETE", "/api/v1/prestaciones/" + id, null, t).status());
    assertEquals(404, request("GET", "/api/v1/prestaciones/" + id, null, t).status());
    assertEquals(409, request("DELETE", "/api/v1/prestaciones/1", null, t).status());
    d.put("codigo", "INVALIDO");
    var invalido = request("POST", "/api/v1/prestaciones", d, t);
    assertEquals(400, invalido.status());
    assertTrue(invalido.body().get("campos").has("codigo"));
  }

  @Test
  void crudMascotasYReglas() throws Exception {
    String t = token("recepcion");
    var d = mascota("Prueba mascota");
    var r = request("POST", "/api/v1/mascotas", d, t);
    assertEquals(201, r.status(), String.valueOf(r.body()));
    long id = r.body().get("id").asLong();
    assertEquals("Rosa Huamán", r.body().get("propietarioNombre").asText());
    assertFalse(r.body().has("propietario"));
    assertEquals(409, request("POST", "/api/v1/mascotas", d, t).status());
    d.put("nombre", "Mascota editada");
    assertEquals(200, request("PUT", "/api/v1/mascotas/" + id, d, t).status());
    assertEquals(204, request("DELETE", "/api/v1/mascotas/" + id, null, t).status());
    assertEquals(409, request("DELETE", "/api/v1/mascotas/1", null, t).status());
    d.put("propietarioId", 99999);
    assertEquals(404, request("POST", "/api/v1/mascotas", d, t).status());
    d.put("fechaNacimiento", LocalDate.now().plusDays(1).toString());
    var invalido = request("POST", "/api/v1/mascotas", d, t);
    assertEquals(400, invalido.status());
    assertTrue(invalido.body().get("campos").has("fechaNacimiento"));
  }

  @Test
  void calculaDescuentoEnBackendEIgnoraImportesDelCliente() throws Exception {
    String t = token("recepcion");
    var d = atencion(List.of(detalle(1, 1), detalle(2, 1), detalle(4, 1)));
    d.put("total", 1);
    d.put("descuento", 999);
    d.put("subtotal", 1);
    var r = request("POST", "/api/v1/atenciones", d, t);
    assertEquals(201, r.status(), String.valueOf(r.body()));
    assertEquals(160, r.body().get("subtotal").asDouble());
    assertEquals(16, r.body().get("descuento").asDouble());
    assertEquals(144, r.body().get("total").asDouble());
    assertEquals(3, r.body().get("detalles").size());
    assertTrue(r.body().get("numero").asText().matches("AT-\\d{6}"));
    var simple = request("POST", "/api/v1/atenciones", atencion(List.of(detalle(1, 2))), t);
    assertEquals(100, simple.body().get("total").asDouble());
    assertEquals(0, simple.body().get("descuento").asDouble());
  }

  @Test
  void tercerDetalleInvalidoNoGuardaCabeceraNiDetalles() throws Exception {
    int cabeceras = jdbc.queryForObject("select count(*) from atencion", Integer.class);
    int detalles = jdbc.queryForObject("select count(*) from detalle_atencion", Integer.class);
    var r =
        request(
            "POST",
            "/api/v1/atenciones",
            atencion(List.of(detalle(1, 1), detalle(4, 1), detalle(3, 1))),
            token("recepcion"));
    assertEquals(409, r.status());
    assertEquals(cabeceras, jdbc.queryForObject("select count(*) from atencion", Integer.class));
    assertEquals(
        detalles, jdbc.queryForObject("select count(*) from detalle_atencion", Integer.class));
  }

  @Test
  void validaDetalleCantidadPesoEInactiva() throws Exception {
    String t = token("recepcion");
    assertEquals(
        400,
        request("POST", "/api/v1/atenciones", atencion(List.of(detalle(1, 1), detalle(1, 1))), t)
            .status());
    assertEquals(
        400, request("POST", "/api/v1/atenciones", atencion(List.of(detalle(1, 4))), t).status());
    assertEquals(400, request("POST", "/api/v1/atenciones", atencion(List.of()), t).status());
    assertEquals(
        400,
        request("POST", "/api/v1/atenciones", atencion(Collections.nCopies(9, detalle(1, 1))), t)
            .status());
    assertEquals(
        409, request("POST", "/api/v1/atenciones", atencion(List.of(detalle(9, 1))), t).status());
    var d = atencion(List.of(detalle(1, 1)));
    d.put("peso", 0);
    assertEquals(400, request("POST", "/api/v1/atenciones", d, t).status());
    d.put("peso", 12.5);
    d.put("detalles", List.of(Map.of("prestacionId", 1, "cantidad", 1.5)));
    assertEquals(400, request("POST", "/api/v1/atenciones", d, t).status());
  }

  @Test
  void anulacionReporteYFiltros() throws Exception {
    String t = token("admin");
    var creada = request("POST", "/api/v1/atenciones", atencion(List.of(detalle(1, 1))), t);
    long id = creada.body().get("id").asLong();
    assertEquals(
        400,
        request("PATCH", "/api/v1/atenciones/" + id + "/anular", Map.of("motivo", "corto"), t)
            .status());
    var anulada =
        request(
            "PATCH",
            "/api/v1/atenciones/" + id + "/anular",
            Map.of("motivo", "Registro duplicado de prueba"),
            token("veterinario"));
    assertEquals(200, anulada.status());
    assertEquals("ANULADA", anulada.body().get("estado").asText());
    assertEquals(
        409,
        request(
                "PATCH",
                "/api/v1/atenciones/" + id + "/anular",
                Map.of("motivo", "Segundo intento de anulación"),
                t)
            .status());
    var antigua = request("POST", "/api/v1/atenciones", atencion(List.of(detalle(1, 1))), t);
    long antiguoId = antigua.body().get("id").asLong();
    jdbc.update(
        "update atencion set fecha=? where id=?",
        java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().minusHours(49)),
        antiguoId);
    assertEquals(
        409,
        request(
                "PATCH",
                "/api/v1/atenciones/" + antiguoId + "/anular",
                Map.of("motivo", "Intento fuera de las 48 horas"),
                t)
            .status());
    var lista =
        request(
            "GET",
            "/api/v1/atenciones?mascota=lUn&especie=PERRO&estado=REGISTRADA&desde="
                + LocalDate.now().minusDays(3)
                + "&hasta="
                + LocalDate.now(),
            null,
            t);
    assertEquals(200, lista.status());
    assertFalse(lista.body().isEmpty());
    for (var a : lista.body()) {
      assertEquals("Luna", a.get("mascotaNombre").asText());
      assertEquals("REGISTRADA", a.get("estado").asText());
    }
    assertTrue(
        request("GET", "/api/v1/atenciones?mascota=sincoincidencias", null, t).body().isEmpty());
    var reporte = request("GET", "/api/v1/reportes/atenciones-por-especie", null, t);
    assertEquals(200, reporte.status());
    for (var fila : reporte.body()) {
      String especie = fila.get("especie").asText();
      BigDecimal total =
          jdbc.queryForObject(
              "select sum(a.total) from atencion a join mascota m on m.id=a.mascota_id where"
                  + " a.estado <> 'ANULADA' and m.especie=?",
              BigDecimal.class,
              especie);
      assertEquals(0, total.compareTo(fila.get("importeTotal").decimalValue()));
    }
  }

  @Test
  void swaggerYCors() throws Exception {
    var doc = request("GET", "/v3/api-docs", null, null);
    assertEquals(200, doc.status());
    assertTrue(doc.body().get("paths").has("/api/v1/atenciones"));
    var req =
        HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/api/v1/mascotas"))
            .header("Origin", "http://localhost:4200")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "Authorization,Content-Type")
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build();
    var r = client.send(req, HttpResponse.BodyHandlers.ofString());
    assertEquals(200, r.statusCode());
    assertEquals(
        "http://localhost:4200", r.headers().firstValue("Access-Control-Allow-Origin").orElse(""));
  }
}
