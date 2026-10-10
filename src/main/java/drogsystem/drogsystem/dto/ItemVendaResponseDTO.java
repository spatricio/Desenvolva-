package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.ItemVenda;

import java.math.BigDecimal;

public record ItemVendaResponseDTO(
        Long id,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal) {
    public static ItemVendaResponseDTO from(ItemVenda item) {
        return new ItemVendaResponseDTO(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubtotal());
    }
}
