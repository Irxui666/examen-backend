package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "mascota")
@Getter
@Setter
public class Mascota {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 40)
  private String nombre;

  @Column(nullable = false, length = 6)
  private String especie;

  @Column(length = 40)
  private String raza;

  @Column(name = "fecha_nacimiento", nullable = false)
  private LocalDate fechaNacimiento;

  @Column(nullable = false, length = 1)
  @JdbcTypeCode(SqlTypes.CHAR)
  private String sexo;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "propietario_id")
  private Propietario propietario;
}
