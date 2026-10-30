package pe.edu.upeu.vetandes.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.vetandes.dto.*;

public interface ReporteService {
  List<AtencionesPorEspecieDTO> porEspecie(LocalDate desde, LocalDate hasta);
}
