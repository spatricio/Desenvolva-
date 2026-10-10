package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.Notificacao;

import java.time.LocalDateTime;

public record NotificacaoResponseDTO(
        Long id,
        String titulo,
        String mensagem,
        boolean lida,
        Long reservaId,
        LocalDateTime dataCriacao) {
    public static NotificacaoResponseDTO from(Notificacao n) {
        return new NotificacaoResponseDTO(
                n.getId(),
                n.getTitulo(),
                n.getMensagem(),
                n.isLida(),
                n.getReserva() != null ? n.getReserva().getId() : null,
                n.getDataCriacao());
    }
}
