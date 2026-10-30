package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "atencion")
@Getter
@Setter
public class Atencion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 9)
  private String numero;

  @Column(nullable = false)
  private LocalDateTime fecha;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "mascota_id")
  private Mascota mascota;

  @Column(nullable = false, precision = 5, scale = 2)
  private BigDecimal peso;

  @Column(length = 300)
  private String diagnostico;

  @Column(nullable = false, length = 12)
  private String estado;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal descuento;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Column(name = "usuario_registro", nullable = false, length = 30)
  private String usuarioRegistro;

  @Column(name = "motivo_anulacion", length = 200)
  private String motivoAnulacion;

  @OneToMany(mappedBy = "atencion", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<DetalleAtencion> detalles = new ArrayList<>();
}
