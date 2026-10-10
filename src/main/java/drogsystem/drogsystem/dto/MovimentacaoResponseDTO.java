package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.MovimentacaoEstoque;
import drogsystem.drogsystem.enums.TipoMovimentacao;

import java.time.LocalDateTime;

public record MovimentacaoResponseDTO(
        Long id,
        Long produtoId,
        String nomeProduto,
        Long loteId,
        String numeroLote,
        TipoMovimentacao tipo,
        Integer quantidade,
        String motivo,
        String usuarioResponsavel,
        LocalDateTime dataMovimentacao
) {
    public static MovimentacaoResponseDTO from(MovimentacaoEstoque m) {
        return new MovimentacaoResponseDTO(
                m.getId(),
                m.getProduto().getId(),
                m.getProduto().getNome(),
                m.getLote() != null ? m.getLote().getId() : null,
                m.getLote() != null ? m.getLote().getNumeroLote() : null,
                m.getTipo(),
                m.getQuantidade(),
                m.getMotivo(),
                m.getUsuarioResponsavel(),
                m.getDataMovimentacao()
        );
    }
}
