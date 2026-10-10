package drogsystem.drogsystem.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EntradaEstoqueDTO(

        @NotNull(message = "Produto é obrigatório") Long produtoId,

        @NotNull(message = "Quantidade é obrigatória") @Min(value = 1, message = "Quantidade deve ser maior que zero") Integer quantidade,

        @NotBlank(message = "Número do lote é obrigatório") String numeroLote,

        @NotNull(message = "Data de validade é obrigatória") @Future(message = "Data de validade deve ser uma data futura") LocalDate dataValidade,

        String motivo) {
}
