package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.time.*;

public record EstadoRequestDTO(@NotNull Boolean activo) {}
