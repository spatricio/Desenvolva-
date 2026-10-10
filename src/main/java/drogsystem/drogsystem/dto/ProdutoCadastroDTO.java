package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.enums.CategoriaProduto;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record ProdutoCadastroDTO(

        @NotBlank(message = "Nome é obrigatório") String nome,

        String descricao,

        String codigoBarras,

        @NotNull(message = "Categoria é obrigatória") CategoriaProduto categoria,

        @NotBlank(message = "Unidade de medida é obrigatória") String unidadeMedida,

        @Min(value = 0, message = "Estoque mínimo não pode ser negativo") Integer estoqueMinimo,

        @NotNull(message = "Preço é obrigatório") @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero") BigDecimal preco) {
}
