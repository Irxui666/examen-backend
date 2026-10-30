package pe.edu.upeu.vetandes.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.*;

@RestController
@RequestMapping("/api/v1/prestaciones")
@RequiredArgsConstructor
public class PrestacionController {
  private final PrestacionService service;

  @GetMapping
  @PreAuthorize("hasAuthority('PRESTACION_LEER')")
  public List<PrestacionResponseDTO> listar(@RequestParam(required = false) String especie) {
    return service.listar(especie);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('PRESTACION_LEER')")
  public PrestacionResponseDTO obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
  public PrestacionResponseDTO crear(@Valid @RequestBody PrestacionRequestDTO d) {
    return service.crear(d);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
  public PrestacionResponseDTO actualizar(
      @PathVariable Long id, @Valid @RequestBody PrestacionRequestDTO d) {
    return service.actualizar(id, d);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }

  @PatchMapping("/{id}/estado")
  @PreAuthorize("hasAuthority('PRESTACION_ESCRIBIR')")
  public PrestacionResponseDTO estado(
      @PathVariable Long id, @Valid @RequestBody EstadoRequestDTO d) {
    return service.estado(id, d.activo());
  }
}
