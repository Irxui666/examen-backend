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
@RequestMapping("/api/v1/mascotas")
@RequiredArgsConstructor
public class MascotaController {
  private final MascotaService service;

  @GetMapping
  @PreAuthorize("hasAuthority('MASCOTA_LEER')")
  public List<MascotaResponseDTO> listar(@RequestParam(required = false) Long propietarioId) {
    return service.listar(propietarioId);
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAuthority('MASCOTA_LEER')")
  public MascotaResponseDTO obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  @PreAuthorize("hasAuthority('MASCOTA_ESCRIBIR')")
  public MascotaResponseDTO crear(@Valid @RequestBody MascotaRequestDTO d) {
    return service.crear(d);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAuthority('MASCOTA_ESCRIBIR')")
  public MascotaResponseDTO actualizar(
      @PathVariable Long id, @Valid @RequestBody MascotaRequestDTO d) {
    return service.actualizar(id, d);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("hasAuthority('MASCOTA_ESCRIBIR')")
  public void eliminar(@PathVariable Long id) {
    service.eliminar(id);
  }
}
