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
public class AtencionServiceImpl implements AtencionService {
  private final AtencionRepository repository;
  private final MascotaRepository mascotas;
  private final PrestacionRepository prestaciones;
  private final VetandesMapper mapper;
  private static final Logger log = LoggerFactory.getLogger(AtencionServiceImpl.class);

  private Atencion entidad(Long id) {
    return repository
        .findById(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Atención no encontrada"));
  }

  @Transactional
  public AtencionResponseDTO registrar(AtencionRequestDTO d, String username) {
    if (d.detalles() == null
        || d.detalles().isEmpty()
        || d.detalles().size() > 8
        || d.peso() == null
        || d.peso().signum() <= 0)
      throw new ReglaNegocioException(
          org.springframework.http.HttpStatus.BAD_REQUEST,
          "Debe indicar peso positivo y de 1 a 8 detalles");
    Mascota mascota =
        mascotas
            .findById(d.mascotaId())
            .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada"));
    Atencion a = new Atencion();
    a.setMascota(mascota);
    a.setPeso(d.peso());
    a.setDiagnostico(d.diagnostico());
    a.setFecha(LocalDateTime.now());
    a.setEstado("REGISTRADA");
    a.setUsuarioRegistro(username);
    Set<Long> ids = new HashSet<>();
    BigDecimal subtotal = BigDecimal.ZERO;
    for (DetalleAtencionRequestDTO linea : d.detalles()) {
      if (linea == null
          || linea.cantidad() == null
          || linea.cantidad() < 1
          || linea.cantidad() > 3
          || !ids.add(linea.prestacionId()))
        throw new ReglaNegocioException(
            org.springframework.http.HttpStatus.BAD_REQUEST,
            "No repita prestaciones y use cantidades de 1 a 3");
      Prestacion p =
          prestaciones
              .findById(linea.prestacionId())
              .orElseThrow(() -> new RecursoNoEncontradoException("Prestación no encontrada"));
      if (!Boolean.TRUE.equals(p.getActivo())
          || !(p.getEspecieAplicable().equals("TODAS")
              || p.getEspecieAplicable().equals(mascota.getEspecie())))
        throw new ReglaNegocioException(
            "La prestación "
                + p.getNombre()
                + " está inactiva o no corresponde a la especie de la mascota");
      DetalleAtencion detalle = new DetalleAtencion();
      detalle.setAtencion(a);
      detalle.setPrestacion(p);
      detalle.setCantidad(linea.cantidad());
      detalle.setPrecioUnitario(p.getPrecio());
      detalle.setSubtotal(
          p.getPrecio()
              .multiply(BigDecimal.valueOf(linea.cantidad()))
              .setScale(2, RoundingMode.HALF_UP));
      a.getDetalles().add(detalle);
      subtotal = subtotal.add(detalle.getSubtotal());
    }
    a.setSubtotal(subtotal.setScale(2, RoundingMode.HALF_UP));
    a.setDescuento(
        (ids.size() >= 3 ? subtotal.multiply(new BigDecimal("0.10")) : BigDecimal.ZERO)
            .setScale(2, RoundingMode.HALF_UP));
    a.setTotal(a.getSubtotal().subtract(a.getDescuento()));
    Long numero = repository.siguienteNumero();
    if (numero > 999999) throw new ReglaNegocioException("Se alcanzó el límite de numeración");
    a.setNumero("AT-%06d".formatted(numero));
    repository.saveAndFlush(a);
    log.info("Alta de atención {} por {}", a.getNumero(), username);
    return mapper.atencion(a);
  }

  public AtencionResponseDTO obtener(Long id) {
    return mapper.atencion(entidad(id));
  }

  @Transactional
  public AtencionResponseDTO anular(Long id, String motivo) {
    if (motivo == null || motivo.trim().length() < 10 || motivo.trim().length() > 200)
      throw new ReglaNegocioException(
          org.springframework.http.HttpStatus.BAD_REQUEST,
          "El motivo debe tener de 10 a 200 caracteres");
    Atencion a =
        repository
            .bloquear(id)
            .orElseThrow(() -> new RecursoNoEncontradoException("Atención no encontrada"));
    if (!a.getEstado().equals("REGISTRADA")
        || a.getFecha().isBefore(LocalDateTime.now().minusHours(48)))
      throw new ReglaNegocioException(
          "Solo se anulan atenciones REGISTRADAS dentro de las 48 horas");
    a.setEstado("ANULADA");
    a.setMotivoAnulacion(motivo.trim());
    log.info("Anulación de atención {}", a.getNumero());
    return mapper.atencion(repository.save(a));
  }

  public List<AtencionResponseDTO> consultar(
      String mascota, String especie, String estado, LocalDate desde, LocalDate hasta) {
    if (desde != null && hasta != null && desde.isAfter(hasta))
      throw new ReglaNegocioException(
          org.springframework.http.HttpStatus.BAD_REQUEST,
          "La fecha desde no puede superar la fecha hasta");
    return repository
        .consultar(
            mascota == null || mascota.isBlank()
                ? null
                : "%"
                    + mascota
                        .trim()
                        .toLowerCase(java.util.Locale.ROOT)
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_")
                    + "%",
            especie,
            estado,
            desde == null ? null : desde.atStartOfDay(),
            hasta == null ? null : hasta.plusDays(1).atStartOfDay())
        .stream()
        .map(mapper::atencion)
        .toList();
  }
}
