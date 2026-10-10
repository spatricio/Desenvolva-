package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.enums.TipoDesconto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PromocaoCadastroDTO(

        @NotBlank(message = "Nome é obrigatório") String nome,

        String descricao,

        @NotNull(message = "Tipo de desconto é obrigatório") TipoDesconto tipoDesconto,

        @NotNull(message = "Valor do desconto é obrigatório") @DecimalMin(value = "0.01", message = "Valor do desconto deve ser maior que zero") BigDecimal valorDesconto,

        @NotNull(message = "Data de início é obrigatória") LocalDate dataInicio,

        @NotNull(message = "Data de fim é obrigatória") LocalDate dataFim) {
}
