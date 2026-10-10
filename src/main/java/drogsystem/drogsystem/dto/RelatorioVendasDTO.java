package drogsystem.drogsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RelatorioVendasDTO(
        LocalDate dataInicio,
        LocalDate dataFim,
        long totalVendasConcluidas,
        long totalVendasCanceladas,
        BigDecimal receitaTotal,
        BigDecimal ticketMedio,
        List<ItemVendasPorDiaDTO> vendasPorDia,
        List<VendasPorFormaPagamentoDTO> vendasPorFormaPagamento,
        List<ProdutoMaisVendidoDTO> topProdutos) {
}
