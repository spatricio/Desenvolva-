package drogsystem.drogsystem.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "lotes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false)
    private String numeroLote;

    @Column(nullable = false)
    private LocalDate dataValidade;

    @Column(nullable = false)
    private Integer quantidadeInicial;

    @Column(nullable = false)
    private Integer quantidadeAtual;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataEntrada;

    @PrePersist
    void prePersist() {
        this.dataEntrada = LocalDateTime.now();
    }
}
