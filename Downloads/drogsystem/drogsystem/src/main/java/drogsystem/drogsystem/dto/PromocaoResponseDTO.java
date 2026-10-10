package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.Promocao;
import drogsystem.drogsystem.enums.TipoDesconto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PromocaoResponseDTO(
        Long id,
        String nome,
        String descricao,
        TipoDesconto tipoDesconto,
        BigDecimal valorDesconto,
        LocalDate dataInicio,
        LocalDate dataFim,
        boolean ativa,
        boolean vigente,
        LocalDateTime dataCadastro) {
    public static PromocaoResponseDTO from(Promocao p) {
        return new PromocaoResponseDTO(
                p.getId(),
                p.getNome(),
                p.getDescricao(),
                p.getTipoDesconto(),
                p.getValorDesconto(),
                p.getDataInicio(),
                p.getDataFim(),
                p.isAtiva(),
                p.isVigente(),
                p.getDataCadastro());
    }
}
