package pe.edu.upeu.vetandes.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.vetandes.entity.Mascota;

public interface MascotaRepository extends JpaRepository<Mascota, Long> {
  boolean existsByNombreIgnoreCaseAndPropietarioIdAndIdNot(
      String nombre, Long propietarioId, Long id);

  List<Mascota> findByPropietarioIdOrderByNombreAsc(Long propietarioId);

  List<Mascota> findAllByOrderByNombreAsc();
}
