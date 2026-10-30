package pe.edu.upeu.vetandes.controller;

import jakarta.validation.Valid;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.service.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthService service;

  @PostMapping("/login")
  @io.swagger.v3.oas.annotations.security.SecurityRequirements
  public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO d) {
    return service.login(d);
  }

  @GetMapping("/me")
  public UsuarioResponseDTO actual(Principal p) {
    return service.actual(p.getName());
  }
}
