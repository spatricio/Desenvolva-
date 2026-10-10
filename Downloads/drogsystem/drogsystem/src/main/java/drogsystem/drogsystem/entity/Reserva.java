package drogsystem.drogsystem.entity;

import drogsystem.drogsystem.enums.StatusReserva;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private Integer quantidade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusReserva status;

    private String observacoes;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataReserva;

    // preenchido quando o produto fica disponível
    private LocalDateTime dataNotificacao;

    // prazo para o cliente confirmar a retirada (3 dias após notificação)
    private LocalDateTime dataExpiracao;

    @PrePersist
    void prePersist() {
        this.dataReserva = LocalDateTime.now();
    }
}
