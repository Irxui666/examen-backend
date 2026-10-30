package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record DetalleAtencionResponseDTO(
    Long id,
    Long prestacionId,
    String prestacionNombre,
    Integer cantidad,
    BigDecimal precioUnitario,
    BigDecimal subtotal) {}
