package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record PrestacionResponseDTO(
    Long id,
    String codigo,
    String nombre,
    BigDecimal precio,
    String especieAplicable,
    Boolean activo) {}
