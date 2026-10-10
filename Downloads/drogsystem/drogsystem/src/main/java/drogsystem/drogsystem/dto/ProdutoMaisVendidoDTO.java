package drogsystem.drogsystem.dto;

import java.math.BigDecimal;

public record ProdutoMaisVendidoDTO(
        Long produtoId,
        String nomeProduto,
        long quantidadeVendida,
        BigDecimal receita) {
}
