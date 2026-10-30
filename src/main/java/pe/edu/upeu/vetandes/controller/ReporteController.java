package pe.edu.upeu.vetandes.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.*;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {
  private final ReporteService service;

  @GetMapping("/atenciones-por-especie")
  @PreAuthorize("hasAuthority('REPORTE_VER')")
  public List<AtencionesPorEspecieDTO> reporte(
      @RequestParam(required = false) LocalDate desde,
      @RequestParam(required = false) LocalDate hasta) {
    return service.porEspecie(desde, hasta);
  }
}
