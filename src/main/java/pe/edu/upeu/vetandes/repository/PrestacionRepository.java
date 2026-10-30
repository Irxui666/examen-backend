package pe.edu.upeu.vetandes.repository;

import java.util.*;
import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.vetandes.entity.Prestacion;

public interface PrestacionRepository extends JpaRepository<Prestacion, Long> {
  boolean existsByCodigoIgnoreCaseAndIdNot(String codigo, Long id);

  List<Prestacion> findByEspecieAplicableInOrderByNombreAsc(List<String> especies);

  List<Prestacion> findAllByOrderByNombreAsc();
}
