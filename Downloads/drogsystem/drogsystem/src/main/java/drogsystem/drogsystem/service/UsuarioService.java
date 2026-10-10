package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.UsuarioCadastroDTO;
import drogsystem.drogsystem.dto.UsuarioResponseDTO;
import drogsystem.drogsystem.entity.ClientePerfil;
import drogsystem.drogsystem.entity.Usuario;
import drogsystem.drogsystem.enums.TipoPerfil;
import drogsystem.drogsystem.repository.ClientePerfilRepository;
import drogsystem.drogsystem.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClientePerfilRepository clientePerfilRepository;

    @Transactional
    public UsuarioResponseDTO cadastrar(UsuarioCadastroDTO dto) {
        if (usuarioRepository.existsByEmail(dto.email())) {
            throw new IllegalArgumentException("E-mail já cadastrado.");
        }
        Usuario usuario = Usuario.builder()
                .nome(dto.nome())
                .email(dto.email())
                .senha(passwordEncoder.encode(dto.senha()))
                .perfil(dto.perfil())
                .build();
        usuario = usuarioRepository.save(usuario);
        if (dto.perfil() == TipoPerfil.CLIENTE) {
            clientePerfilRepository.save(ClientePerfil.builder().usuario(usuario).build());
        }
        return UsuarioResponseDTO.from(usuario);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .map(UsuarioResponseDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
    }

    @Transactional
    public void desativar(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        usuario.setAtivo(false);
        usuarioRepository.save(usuario);
    }
}
