package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.Lote;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record LoteResponseDTO(
        Long id,
        Long produtoId,
        String nomeProduto,
        String numeroLote,
        LocalDate dataValidade,
        Integer quantidadeInicial,
        Integer quantidadeAtual,
        boolean vencido,
        LocalDateTime dataEntrada) {
    public static LoteResponseDTO from(Lote lote) {
        return new LoteResponseDTO(
                lote.getId(),
                lote.getProduto().getId(),
                lote.getProduto().getNome(),
                lote.getNumeroLote(),
                lote.getDataValidade(),
                lote.getQuantidadeInicial(),
                lote.getQuantidadeAtual(),
                lote.getDataValidade().isBefore(LocalDate.now()),
                lote.getDataEntrada());
    }
}
