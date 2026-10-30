package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
public class Usuario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 30)
  private String username;

  @Column(length = 100)
  private String password;

  @Column(name = "nombre_completo", nullable = false, length = 120)
  private String nombreCompleto;

  @Column(nullable = false)
  private Boolean activo;

  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
      name = "usuario_roles",
      joinColumns = @JoinColumn(name = "usuario_id"),
      inverseJoinColumns = @JoinColumn(name = "rol_id"))
  private Set<Rol> roles = new HashSet<>();
}
