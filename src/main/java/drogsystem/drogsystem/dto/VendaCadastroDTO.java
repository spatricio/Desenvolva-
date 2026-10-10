package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.enums.FormaPagamento;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record VendaCadastroDTO(

        Long clienteId,

        @NotEmpty(message = "A venda deve ter ao menos um item") List<@Valid ItemVendaDTO> itens,

        @NotNull(message = "Forma de pagamento é obrigatória") FormaPagamento formaPagamento,

        @NotNull(message = "Valor pago é obrigatório") @DecimalMin(value = "0.0", inclusive = false, message = "Valor pago deve ser maior que zero") BigDecimal valorPago,

        Long promocaoId,

        @Min(value = 0, message = "Pontos para resgatar não pode ser negativo") Integer pontosParaResgatar,

        String observacoes) {
}
