package pe.edu.upeu.vetandes.service;

import java.util.List;
import pe.edu.upeu.vetandes.dto.*;

public interface MascotaService {
  List<MascotaResponseDTO> listar(Long propietarioId);

  MascotaResponseDTO obtener(Long id);

  MascotaResponseDTO crear(MascotaRequestDTO d);

  MascotaResponseDTO actualizar(Long id, MascotaRequestDTO d);

  void eliminar(Long id);

  List<PropietarioResponseDTO> propietarios();
}
