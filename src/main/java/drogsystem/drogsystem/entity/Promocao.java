package drogsystem.drogsystem.entity;

import drogsystem.drogsystem.enums.TipoDesconto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promocoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoDesconto tipoDesconto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDesconto;

    @Column(nullable = false)
    private LocalDate dataInicio;

    @Column(nullable = false)
    private LocalDate dataFim;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativa = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    void prePersist() {
        this.dataCadastro = LocalDateTime.now();
    }

    public boolean isVigente() {
        LocalDate hoje = LocalDate.now();
        return ativa && !hoje.isBefore(dataInicio) && !hoje.isAfter(dataFim);
    }
}
