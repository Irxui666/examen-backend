package pe.edu.upeu.vetandes.security;

import java.util.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import pe.edu.upeu.vetandes.entity.Rol;

public final class PermisosPorRol {
  private static final Map<String, Set<Permiso>> PERMISOS =
      Map.of(
          "ADMIN", EnumSet.allOf(Permiso.class),
          "RECEPCIONISTA",
              EnumSet.of(
                  Permiso.PRESTACION_LEER,
                  Permiso.MASCOTA_LEER,
                  Permiso.MASCOTA_ESCRIBIR,
                  Permiso.ATENCION_LEER,
                  Permiso.ATENCION_REGISTRAR),
          "VETERINARIO",
              EnumSet.of(
                  Permiso.PRESTACION_LEER,
                  Permiso.PRESTACION_ESCRIBIR,
                  Permiso.MASCOTA_LEER,
                  Permiso.ATENCION_LEER,
                  Permiso.ATENCION_ANULAR,
                  Permiso.REPORTE_VER));

  private PermisosPorRol() {}

  public static List<String> permisos(Collection<Rol> roles) {
    return roles.stream()
        .flatMap(r -> PERMISOS.getOrDefault(r.getNombre(), Set.of()).stream())
        .map(Enum::name)
        .distinct()
        .sorted()
        .toList();
  }

  public static List<GrantedAuthority> authorities(Collection<Rol> roles) {
    return permisos(roles).stream()
        .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p))
        .toList();
  }
}
