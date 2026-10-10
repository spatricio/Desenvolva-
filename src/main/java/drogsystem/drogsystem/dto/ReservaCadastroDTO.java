package drogsystem.drogsystem.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReservaCadastroDTO(

        @NotNull(message = "Produto é obrigatório") Long produtoId,

        @NotNull(message = "Quantidade é obrigatória") @Min(value = 1, message = "Quantidade deve ser maior que zero") Integer quantidade,

        String observacoes) {
}
