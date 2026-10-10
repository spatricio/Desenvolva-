package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.ClientePerfil;
import drogsystem.drogsystem.enums.NivelFidelidade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ClientePerfilResponseDTO(
        Long usuarioId,
        String nome,
        String email,
        String cpf,
        String telefone,
        LocalDate dataNascimento,
        String endereco,
        Integer pontosAcumulados,
        BigDecimal descontoDisponivel,
        BigDecimal totalGasto,
        NivelFidelidade nivel,
        LocalDateTime dataUltimaCompra) {
    public static ClientePerfilResponseDTO from(ClientePerfil p) {
        // cada 100 pontos = R$ 5,00 de desconto
        BigDecimal desconto = BigDecimal.valueOf((long) (p.getPontosAcumulados() / 100) * 5);
        return new ClientePerfilResponseDTO(
                p.getUsuario().getId(),
                p.getUsuario().getNome(),
                p.getUsuario().getEmail(),
                p.getCpf(),
                p.getTelefone(),
                p.getDataNascimento(),
                p.getEndereco(),
                p.getPontosAcumulados(),
                desconto,
                p.getTotalGasto(),
                p.getNivel(),
                p.getDataUltimaCompra());
    }
}
