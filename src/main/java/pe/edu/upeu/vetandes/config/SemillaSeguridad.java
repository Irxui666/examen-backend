package pe.edu.upeu.vetandes.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.repository.UsuarioRepository;

@Component
@RequiredArgsConstructor
public class SemillaSeguridad implements CommandLineRunner {
  private final UsuarioRepository repository;
  private final PasswordEncoder encoder;

  @Override
  @Transactional
  public void run(String... args) {
    repository.findAll().stream()
        .filter(u -> u.getPassword() == null || u.getPassword().isBlank())
        .forEach(
            u -> {
              u.setPassword(encoder.encode("Vet2026*"));
              repository.save(u);
            });
  }
}
