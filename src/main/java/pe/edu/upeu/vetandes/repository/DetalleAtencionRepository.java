package pe.edu.upeu.vetandes.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.vetandes.entity.DetalleAtencion;

public interface DetalleAtencionRepository extends JpaRepository<DetalleAtencion, Long> {
  boolean existsByPrestacionId(Long prestacionId);
}
