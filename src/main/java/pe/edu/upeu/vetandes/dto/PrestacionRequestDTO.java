package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record PrestacionRequestDTO(
    @NotBlank @Pattern(regexp = "PRS-[0-9]{3}", message = "Use el formato PRS-000") String codigo,
    @NotBlank @Size(min = 3, max = 80) String nombre,
    @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 8, fraction = 2)
        BigDecimal precio,
    @NotBlank @Pattern(regexp = "PERRO|GATO|TODAS") String especieAplicable) {}
