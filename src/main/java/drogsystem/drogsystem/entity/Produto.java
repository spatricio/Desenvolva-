package drogsystem.drogsystem.entity;

import drogsystem.drogsystem.enums.CategoriaProduto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(unique = true)
    private String codigoBarras;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoriaProduto categoria;

    @Column(nullable = false)
    private String unidadeMedida;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantidadeEstoque = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer estoqueMinimo = 0;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCadastro;

    @PrePersist
    void prePersist() {
        this.dataCadastro = LocalDateTime.now();
    }
}
