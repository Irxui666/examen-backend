package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.List;

public record AtencionResponseDTO(
    Long id,
    String numero,
    LocalDateTime fecha,
    Long mascotaId,
    String mascotaNombre,
    String especie,
    String propietarioNombre,
    BigDecimal peso,
    String diagnostico,
    String estado,
    BigDecimal subtotal,
    BigDecimal descuento,
    BigDecimal total,
    String usuarioRegistro,
    String motivoAnulacion,
    List<DetalleAtencionResponseDTO> detalles) {}
