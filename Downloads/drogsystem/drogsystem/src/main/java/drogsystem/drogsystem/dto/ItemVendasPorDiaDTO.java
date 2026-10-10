package drogsystem.drogsystem.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ItemVendasPorDiaDTO(
        LocalDate data,
        long totalVendas,
        BigDecimal receita) {
}
