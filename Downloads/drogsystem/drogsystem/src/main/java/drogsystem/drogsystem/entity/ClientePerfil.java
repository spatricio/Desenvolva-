package drogsystem.drogsystem.entity;

import drogsystem.drogsystem.enums.NivelFidelidade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes_perfil")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientePerfil {

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String cpf;
    private String telefone;
    private LocalDate dataNascimento;
    private String endereco;

    @Builder.Default
    @Column(nullable = false)
    private Integer pontosAcumulados = 0;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalGasto = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(nullable = false)
    private NivelFidelidade nivel = NivelFidelidade.BRONZE;

    private LocalDateTime dataUltimaCompra;
}
