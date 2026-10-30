package pe.edu.upeu.vetandes.repository;

import java.time.LocalDateTime;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import pe.edu.upeu.vetandes.entity.Atencion;

public interface AtencionRepository extends JpaRepository<Atencion, Long> {
  boolean existsByMascotaId(Long mascotaId);

  @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @Query("select a from Atencion a where a.id = :id")
  Optional<Atencion> bloquear(@Param("id") Long id);

  @Query(value = "SELECT sec_atencion.NEXTVAL FROM dual", nativeQuery = true)
  Long siguienteNumero();

  @Query(
      "select a from Atencion a where (:mascota is null or lower(a.mascota.nombre) like"
          + " :mascota escape '!') and (:especie is null or a.mascota.especie ="
          + " :especie) and (:estado is null or a.estado = :estado) and (:desde is null or a.fecha"
          + " >= :desde) and (:hasta is null or a.fecha < :hasta) order by a.fecha desc")
  List<Atencion> consultar(
      @Param("mascota") String mascota,
      @Param("especie") String especie,
      @Param("estado") String estado,
      @Param("desde") LocalDateTime desde,
      @Param("hasta") LocalDateTime hasta);

  @Query(
      "select new pe.edu.upeu.vetandes.dto.AtencionesPorEspecieDTO(a.mascota.especie, count(a),"
          + " sum(a.total)) from Atencion a where a.estado <> 'ANULADA' and (:desde is null or"
          + " a.fecha >= :desde) and (:hasta is null or a.fecha < :hasta) group by"
          + " a.mascota.especie order by sum(a.total) desc")
  List<pe.edu.upeu.vetandes.dto.AtencionesPorEspecieDTO> reporte(
      @Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
