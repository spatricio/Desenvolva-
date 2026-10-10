package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.enums.FormaPagamento;

import java.math.BigDecimal;

public record VendasPorFormaPagamentoDTO(
        FormaPagamento formaPagamento,
        long totalVendas,
        BigDecimal receita) {
}
