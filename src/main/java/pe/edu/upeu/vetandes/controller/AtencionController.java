package pe.edu.upeu.vetandes.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.*;

@RestController
@RequestMapping("/api/v1/atenciones")
@RequiredArgsConstructor
public class AtencionController {
  private final AtencionService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('ATENCION_REGISTRAR')")
  public AtencionResponseDTO registrar(@Valid @RequestBody AtencionRequestDTO d, Principal p) {
    return service.registrar(d, p.getName());
  }

  @GetMapping
  @PreAuthorize("hasAuthority('ATENCION_LEER')")
  public List<AtencionResponseDTO> consultar(
      @RequestParam(required = false) String mascota,
      @RequestParam(required = false) String especie,
      @RequestParam(required = false) String estado,
      @RequestParam(required = false) LocalDate desde,
      @RequestParam(required = false) LocalDate hasta) {
    return service.consultar(mascota, especie, estado, desde, hasta);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('ATENCION_LEER')")
  public AtencionResponseDTO obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @PatchMapping("/{id}/anular")
  @PreAuthorize("hasAuthority('ATENCION_ANULAR')")
  public AtencionResponseDTO anular(@PathVariable Long id, @Valid @RequestBody AnularRequestDTO d) {
    return service.anular(id, d.motivo());
  }
}
