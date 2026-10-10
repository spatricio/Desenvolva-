package drogsystem.drogsystem.dto;

import drogsystem.drogsystem.entity.Usuario;
import drogsystem.drogsystem.enums.TipoPerfil;

import java.time.LocalDateTime;

public record UsuarioResponseDTO(
        Long id,
        String nome,
        String email,
        TipoPerfil perfil,
        boolean ativo,
        LocalDateTime dataCadastro) {
    public static UsuarioResponseDTO from(Usuario usuario) {
        return new UsuarioResponseDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfil(),
                usuario.isAtivo(),
                usuario.getDataCadastro());
    }
}
