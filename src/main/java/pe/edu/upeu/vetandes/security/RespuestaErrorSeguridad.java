package pe.edu.upeu.vetandes.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.springframework.http.MediaType;

final class RespuestaErrorSeguridad {

  private RespuestaErrorSeguridad() {}

  static void escribir(
      HttpServletRequest request,
      HttpServletResponse response,
      int status,
      String error,
      String mensaje)
      throws IOException {

    String json =
        "{"
            + "\"timestamp\":\""
            + LocalDateTime.now()
            + "\","
            + "\"status\":"
            + status
            + ","
            + "\"error\":\""
            + escapar(error)
            + "\","
            + "\"mensaje\":\""
            + escapar(mensaje)
            + "\","
            + "\"ruta\":\""
            + escapar(request.getRequestURI())
            + "\""
            + "}";

    response.setStatus(status);
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.getWriter().write(json);
  }

  private static String escapar(String texto) {
    if (texto == null) {
      return "";
    }
    return texto.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
