package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "propietario")
@Getter
@Setter
public class Propietario {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 8)
  private String dni;

  @Column(nullable = false, length = 120)
  private String nombres;

  @Column(length = 15)
  private String telefono;
}
