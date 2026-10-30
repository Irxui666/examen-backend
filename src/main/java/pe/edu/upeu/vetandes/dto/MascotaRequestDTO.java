package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record MascotaRequestDTO(
    @NotBlank @Size(min = 2, max = 40) String nombre,
    @NotBlank @Pattern(regexp = "PERRO|GATO|OTRO") String especie,
    @Size(max = 40) String raza,
    @NotNull @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
        LocalDate fechaNacimiento,
    @NotBlank @Pattern(regexp = "M|H") String sexo,
    @NotNull @Positive Long propietarioId) {}
