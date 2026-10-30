package pe.edu.upeu.vetandes.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "detalle_atencion")
@Getter
@Setter
public class DetalleAtencion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "atencion_id")
  private Atencion atencion;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "prestacion_id")
  private Prestacion prestacion;

  @Column(nullable = false)
  private Integer cantidad;

  @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
  private BigDecimal precioUnitario;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal subtotal;
}
