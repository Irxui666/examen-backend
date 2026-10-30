package pe.edu.upeu.vetandes.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import pe.edu.upeu.vetandes.dto.ErrorResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ResponseEntity<ErrorResponseDTO> error(
      HttpStatus s, String mensaje, Map<String, String> campos, HttpServletRequest r) {
    if (s.is4xxClientError()) log.warn("{} {}: {}", r.getMethod(), r.getRequestURI(), mensaje);
    return ResponseEntity.status(s)
        .body(
            new ErrorResponseDTO(
                LocalDateTime.now(),
                s.value(),
                s.getReasonPhrase(),
                mensaje,
                r.getRequestURI(),
                campos));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponseDTO> validacion(
      MethodArgumentNotValidException e, HttpServletRequest r) {
    Map<String, String> campos = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
    return error(HttpStatus.BAD_REQUEST, "Datos inválidos", campos, r);
  }

  @ExceptionHandler(ReglaNegocioException.class)
  public ResponseEntity<ErrorResponseDTO> regla(ReglaNegocioException e, HttpServletRequest r) {
    return error(e.getStatus(), e.getMessage(), null, r);
  }

  @ExceptionHandler(RecursoNoEncontradoException.class)
  public ResponseEntity<ErrorResponseDTO> noEncontrado(
      RecursoNoEncontradoException e, HttpServletRequest r) {
    return error(HttpStatus.NOT_FOUND, e.getMessage(), null, r);
  }

  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponseDTO> login(AuthenticationException e, HttpServletRequest r) {
    return error(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", null, r);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponseDTO> prohibido(AccessDeniedException e, HttpServletRequest r) {
    return error(HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación", null, r);
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  public ResponseEntity<ErrorResponseDTO> formato(Exception e, HttpServletRequest r) {
    return error(HttpStatus.BAD_REQUEST, "Formato de datos inválido", null, r);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponseDTO> integridad(Exception e, HttpServletRequest r) {
    return error(
        HttpStatus.CONFLICT, "El registro está duplicado o tiene registros relacionados", null, r);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponseDTO> inesperado(Exception e, HttpServletRequest r) {
    log.error("Error inesperado", e);
    return error(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado", null, r);
  }

  @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
  public ResponseEntity<ErrorResponseDTO> rutaNoEncontrada(Exception e, HttpServletRequest r) {
    return error(HttpStatus.NOT_FOUND, "Recurso no encontrado", null, r);
  }
}
