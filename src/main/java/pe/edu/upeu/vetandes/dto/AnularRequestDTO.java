package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record AnularRequestDTO(@NotBlank @Size(min = 10, max = 200) String motivo) {}
