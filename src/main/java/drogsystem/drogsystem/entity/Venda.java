package drogsystem.drogsystem.entity;

import drogsystem.drogsystem.enums.FormaPagamento;
import drogsystem.drogsystem.enums.StatusVenda;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "vendas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Venda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Usuario cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "atendente_id", nullable = false)
    private Usuario atendente;

    @Builder.Default
    @OneToMany(mappedBy = "venda", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemVenda> itens = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promocao_id")
    private Promocao promocao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal desconto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalFinal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormaPagamento formaPagamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal troco;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusVenda status;

    private String observacoes;

    @Column(unique = true)
    private String numeroCupom;

    @Builder.Default
    @Column(nullable = false)
    private Integer pontosGerados = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer pontosResgatados = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime dataVenda;

    @PrePersist
    void prePersist() {
        this.dataVenda = LocalDateTime.now();
    }
}
