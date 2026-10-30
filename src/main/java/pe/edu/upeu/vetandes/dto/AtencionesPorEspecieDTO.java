package pe.edu.upeu.vetandes.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.*;

public record AtencionesPorEspecieDTO(
    String especie, Long cantidadAtenciones, BigDecimal importeTotal) {}
