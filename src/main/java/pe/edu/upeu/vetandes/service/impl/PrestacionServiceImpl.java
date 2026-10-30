package pe.edu.upeu.vetandes.service.impl;

import java.math.*;
import java.time.*;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.vetandes.dto.*;
import pe.edu.upeu.vetandes.entity.*;
import pe.edu.upeu.vetandes.exception.*;
import pe.edu.upeu.vetandes.mapper.VetandesMapper;
import pe.edu.upeu.vetandes.repository.*;
import pe.edu.upeu.vetandes.service.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrestacionServiceImpl implements PrestacionService {
  private final PrestacionRepository repository;
  private final DetalleAtencionRepository detalles;
  private final VetandesMapper mapper;
  private static final Logger log = LoggerFactory.getLogger(PrestacionServiceImpl.class);

  private Prestacion entidad(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Prestación no encontrada"));
  }

  public List<PrestacionResponseDTO> listar(String especie) {
    return (especie == null
            ? repository.findAllByOrderByNombreAsc()
            : repository.findByEspecieAplicableInOrderByNombreAsc(List.of(especie, "TODAS")))
        .stream().map(mapper::prestacion).toList();
  }

  public PrestacionResponseDTO obtener(Long id) {
    return mapper.prestacion(entidad(id));
  }

  private Prestacion guardar(Prestacion p, PrestacionRequestDTO d) {
    if (repository.existsByCodigoIgnoreCaseAndIdNot(d.codigo(), p.getId() == null ? 0L : p.getId()))
      throw new ReglaNegocioException("El código de prestación ya existe");
    p.setCodigo(d.codigo());
    p.setNombre(d.nombre().trim());
    p.setPrecio(d.precio());
    p.setEspecieAplicable(d.especieAplicable());
    return repository.saveAndFlush(p);
  }

  @Transactional
  public PrestacionResponseDTO crear(PrestacionRequestDTO d) {
    Prestacion p = guardar(new Prestacion(), d);
    log.info("Alta de prestación {}", p.getId());
    return mapper.prestacion(p);
  }

  @Transactional
  public PrestacionResponseDTO actualizar(Long id, PrestacionRequestDTO d) {
    Prestacion p = guardar(entidad(id), d);
    log.info("Cambio de prestación {}", id);
    return mapper.prestacion(p);
  }

  @Transactional
  public void eliminar(Long id) {
    Prestacion p = entidad(id);
    if (detalles.existsByPrestacionId(id))
      throw new ReglaNegocioException(
          "La prestación tiene atenciones. Utilice la opción de dar de baja");
    repository.delete(p);
    repository.flush();
    log.info("Eliminación de prestación {}", id);
  }

  @Transactional
  public PrestacionResponseDTO estado(Long id, boolean activo) {
    Prestacion p = entidad(id);
    p.setActivo(activo);
    log.info("Estado de prestación {}: {}", id, activo);
    return mapper.prestacion(repository.save(p));
  }
}
