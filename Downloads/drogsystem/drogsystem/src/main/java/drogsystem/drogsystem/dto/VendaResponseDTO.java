package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.Venda;
import drogsystem.drogsystem.enums.FormaPagamento;
import drogsystem.drogsystem.enums.StatusVenda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record VendaResponseDTO(
        Long id,
        String numeroCupom,
        Long clienteId,
        String clienteNome,
        String atendenteNome,
        List<ItemVendaResponseDTO> itens,
        BigDecimal subtotal,
        BigDecimal desconto,
        BigDecimal totalFinal,
        FormaPagamento formaPagamento,
        BigDecimal valorPago,
        BigDecimal troco,
        StatusVenda status,
        String observacoes,
        Integer pontosGerados,
        Integer pontosResgatados,
        LocalDateTime dataVenda) {
    public static VendaResponseDTO from(Venda v) {
        return new VendaResponseDTO(
                v.getId(),
                v.getNumeroCupom(),
                v.getCliente() != null ? v.getCliente().getId() : null,
                v.getCliente() != null ? v.getCliente().getNome() : "Consumidor Final",
                v.getAtendente().getNome(),
                v.getItens().stream().map(ItemVendaResponseDTO::from).toList(),
                v.getSubtotal(),
                v.getDesconto(),
                v.getTotalFinal(),
                v.getFormaPagamento(),
                v.getValorPago(),
                v.getTroco(),
                v.getStatus(),
                v.getObservacoes(),
                v.getPontosGerados(),
                v.getPontosResgatados(),
                v.getDataVenda());
    }
}
