package pe.edu.upeu.vetandes.service;

import java.util.List;
import pe.edu.upeu.vetandes.dto.*;

public interface PrestacionService {
  List<PrestacionResponseDTO> listar(String especie);

  PrestacionResponseDTO obtener(Long id);

  PrestacionResponseDTO crear(PrestacionRequestDTO d);

  PrestacionResponseDTO actualizar(Long id, PrestacionRequestDTO d);

  void eliminar(Long id);

  PrestacionResponseDTO estado(Long id, boolean activo);
}
