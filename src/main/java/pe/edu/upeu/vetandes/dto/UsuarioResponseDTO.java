package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.time.*;
import java.util.List;

public record UsuarioResponseDTO(
    String username, String nombreCompleto, List<String> roles, List<String> permisos) {}
