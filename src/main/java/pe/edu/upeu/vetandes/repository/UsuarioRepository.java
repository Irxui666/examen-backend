package pe.edu.upeu.vetandes.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.vetandes.entity.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
  @EntityGraph(attributePaths = "roles")
  Optional<Usuario> findByUsernameIgnoreCase(String username);
}
