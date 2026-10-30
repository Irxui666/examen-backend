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
public class MascotaServiceImpl implements MascotaService {
  private final MascotaRepository repository;
  private final PropietarioRepository propietarios;
  private final AtencionRepository atenciones;
  private final VetandesMapper mapper;
  private static final Logger log = LoggerFactory.getLogger(MascotaServiceImpl.class);

  private Mascota entidad(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada"));
  }

  public List<MascotaResponseDTO> listar(Long propietarioId) {
    return (propietarioId == null
            ? repository.findAllByOrderByNombreAsc()
            : repository.findByPropietarioIdOrderByNombreAsc(propietarioId))
        .stream().map(mapper::mascota).toList();
  }

  public List<PropietarioResponseDTO> propietarios() {
    return propietarios.findAll(org.springframework.data.domain.Sort.by("nombres")).stream()
        .map(mapper::propietario)
        .toList();
  }

  public MascotaResponseDTO obtener(Long id) {
    return mapper.mascota(entidad(id));
  }

  private Mascota guardar(Mascota m, MascotaRequestDTO d) {
    Propietario p =
        propietarios
            .findById(d.propietarioId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Propietario no encontrado"));
    if (repository.existsByNombreIgnoreCaseAndPropietarioIdAndIdNot(
        d.nombre().trim(), d.propietarioId(), m.getId() == null ? 0L : m.getId()))
      throw new ReglaNegocioException("El propietario ya tiene una mascota con ese nombre");
    m.setNombre(d.nombre().trim());
    m.setEspecie(d.especie());
    m.setRaza(d.raza());
    m.setFechaNacimiento(d.fechaNacimiento());
    m.setSexo(d.sexo());
    m.setPropietario(p);
    return repository.saveAndFlush(m);
  }

  @Transactional
  public MascotaResponseDTO crear(MascotaRequestDTO d) {
    Mascota m = guardar(new Mascota(), d);
    log.info("Alta de mascota {}", m.getId());
    return mapper.mascota(m);
  }

  @Transactional
  public MascotaResponseDTO actualizar(Long id, MascotaRequestDTO d) {
    Mascota m = guardar(entidad(id), d);
    log.info("Cambio de mascota {}", id);
    return mapper.mascota(m);
  }

  @Transactional
  public void eliminar(Long id) {
    Mascota m = entidad(id);
    if (atenciones.existsByMascotaId(id))
      throw new ReglaNegocioException("No se puede eliminar una mascota con atenciones");
    repository.delete(m);
    repository.flush();
    log.info("Eliminación de mascota {}", id);
  }
}
