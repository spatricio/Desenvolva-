package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.Reserva;
import drogsystem.drogsystem.enums.StatusReserva;

import java.time.LocalDateTime;

public record ReservaResponseDTO(
        Long id,
        Long clienteId,
        String clienteNome,
        Long produtoId,
        String nomeProduto,
        Integer quantidade,
        StatusReserva status,
        String observacoes,
        LocalDateTime dataReserva,
        LocalDateTime dataNotificacao,
        LocalDateTime dataExpiracao) {
    public static ReservaResponseDTO from(Reserva r) {
        return new ReservaResponseDTO(
                r.getId(),
                r.getCliente().getId(),
                r.getCliente().getNome(),
                r.getProduto().getId(),
                r.getProduto().getNome(),
                r.getQuantidade(),
                r.getStatus(),
                r.getObservacoes(),
                r.getDataReserva(),
                r.getDataNotificacao(),
                r.getDataExpiracao());
    }
}
