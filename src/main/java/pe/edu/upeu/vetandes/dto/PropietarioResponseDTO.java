package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record PropietarioResponseDTO(Long id, String dni, String nombres, String telefono) {}
