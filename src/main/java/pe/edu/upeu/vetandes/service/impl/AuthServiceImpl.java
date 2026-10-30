package pe.edu.upeu.vetandes.service.impl;

import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.config.JwtService;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.*;
import pe.edu.upeu.vetandes.exception.*;
import pe.edu.upeu.vetandes.repository.*;
import pe.edu.upeu.vetandes.security.*;
import pe.edu.upeu.vetandes.service.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {
  private final UsuarioRepository repository;
  private final PasswordEncoder encoder;
  private final UsuarioDetailsService users;
  private final JwtService jwt;

  public LoginResponseDTO login(LoginRequestDTO d) {
    var u = repository.findByUsernameIgnoreCase(d.username().trim());
    String hash =
        u.map(Usuario::getPassword)
            .orElse("$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy");
    boolean coincide = encoder.matches(d.password(), hash);
    if (u.isEmpty() || !coincide || !Boolean.TRUE.equals(u.get().getActivo()))
      throw new BadCredentialsException("Usuario o contraseña incorrectos");
    Usuario usuario = u.get();
    logLogin(usuario.getUsername());
    return new LoginResponseDTO(
        jwt.generarToken(users.loadUserByUsername(usuario.getUsername())),
        "Bearer",
        jwt.getExpiracionSegundos(),
        usuario.getUsername(),
        usuario.getNombreCompleto(),
        usuario.getRoles().stream().map(Rol::getNombre).sorted().toList(),
        PermisosPorRol.permisos(usuario.getRoles()));
  }

  private void logLogin(String username) {
    LoggerFactory.getLogger(AuthServiceImpl.class).info("Login de {}", username);
  }

  public UsuarioResponseDTO actual(String username) {
    Usuario u =
        repository
            .findByUsernameIgnoreCase(username)
            .orElseThrow(() -> new BadCredentialsException("Usuario o contraseña incorrectos"));
    return new UsuarioResponseDTO(
        u.getUsername(),
        u.getNombreCompleto(),
        u.getRoles().stream().map(Rol::getNombre).sorted().toList(),
        PermisosPorRol.permisos(u.getRoles()));
  }
}
