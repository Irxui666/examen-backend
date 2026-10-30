package pe.edu.upeu.vetandes.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.vetandes.dto.*;

public interface AtencionService {
  AtencionResponseDTO registrar(AtencionRequestDTO d, String username);

  AtencionResponseDTO obtener(Long id);

  AtencionResponseDTO anular(Long id, String motivo);

  List<AtencionResponseDTO> consultar(
      String mascota, String especie, String estado, LocalDate desde, LocalDate hasta);
}
