package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.Produto;
import drogsystem.drogsystem.enums.CategoriaProduto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        String codigoBarras,
        CategoriaProduto categoria,
        String unidadeMedida,
        Integer quantidadeEstoque,
        Integer estoqueMinimo,
        BigDecimal preco,
        boolean ativo,
        boolean abaixoDoEstoqueMinimo,
        LocalDateTime dataCadastro) {
    public static ProdutoResponseDTO from(Produto p) {
        return new ProdutoResponseDTO(
                p.getId(),
                p.getNome(),
                p.getDescricao(),
                p.getCodigoBarras(),
                p.getCategoria(),
                p.getUnidadeMedida(),
                p.getQuantidadeEstoque(),
                p.getEstoqueMinimo(),
                p.getPreco(),
                p.isAtivo(),
                p.getQuantidadeEstoque() < p.getEstoqueMinimo(),
                p.getDataCadastro());
    }
}
