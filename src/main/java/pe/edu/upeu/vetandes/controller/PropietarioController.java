package pe.edu.upeu.vetandes.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.*;

@RestController
@RequestMapping("/api/v1/propietarios")
@RequiredArgsConstructor
public class PropietarioController {
  private final MascotaService service;

  @GetMapping
  @PreAuthorize("hasAuthority('MASCOTA_LEER')")
  public List<PropietarioResponseDTO> listar() {
    return service.propietarios();
  }
}
