package drogsystem.drogsystem.dto;

import java.util.List;

public record RelatorioEstoqueDTO(
        int totalProdutosAtivos,
        int totalProdutosAbaixoMinimo,
        int totalLotesVencidos,
        int totalLotesAVencer30Dias,
        List<ProdutoResponseDTO> produtosAbaixoMinimo,
        List<LoteResponseDTO> lotesVencidos,
        List<LoteResponseDTO> lotesAVencer30Dias
) {}
