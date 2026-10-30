package pe.edu.upeu.vetandes.service;

import pe.edu.upeu.vetandes.dto.*;

public interface AuthService {
  LoginResponseDTO login(LoginRequestDTO d);

  UsuarioResponseDTO actual(String username);
}
