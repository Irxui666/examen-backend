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
import pe.edu.upeu.vetandes.repository.*;
import pe.edu.upeu.vetandes.service.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
  private final AtencionRepository repository;

  public List<AtencionesPorEspecieDTO> porEspecie(LocalDate desde, LocalDate hasta) {
    if (desde != null && hasta != null && desde.isAfter(hasta))
      throw new ReglaNegocioException(
          org.springframework.http.HttpStatus.BAD_REQUEST,
          "La fecha desde no puede superar la fecha hasta");
    return repository.reporte(
        desde == null ? null : desde.atStartOfDay(),
        hasta == null ? null : hasta.plusDays(1).atStartOfDay());
  }
}
