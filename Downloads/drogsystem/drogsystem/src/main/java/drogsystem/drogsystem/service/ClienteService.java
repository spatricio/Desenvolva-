package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.ClientePerfilDTO;
import drogsystem.drogsystem.dto.ClientePerfilResponseDTO;
import drogsystem.drogsystem.dto.VendaResponseDTO;
import drogsystem.drogsystem.entity.ClientePerfil;
import drogsystem.drogsystem.entity.Usuario;
import drogsystem.drogsystem.repository.ClientePerfilRepository;
import drogsystem.drogsystem.repository.UsuarioRepository;
import drogsystem.drogsystem.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClientePerfilRepository clientePerfilRepository;
    private final UsuarioRepository usuarioRepository;
    private final VendaRepository vendaRepository;

    @Transactional(readOnly = true)
    public List<ClientePerfilResponseDTO> listarClientes() {
        return clientePerfilRepository.findAll().stream()
                .map(ClientePerfilResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ClientePerfilResponseDTO buscarPorId(Long id) {
        return clientePerfilRepository.findById(id)
                .map(ClientePerfilResponseDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de cliente não encontrado."));
    }

    @Transactional
    public ClientePerfilResponseDTO atualizarPerfil(Long usuarioId, ClientePerfilDTO dto) {
        ClientePerfil perfil = clientePerfilRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de cliente não encontrado."));
        perfil.setCpf(dto.cpf());
        perfil.setTelefone(dto.telefone());
        perfil.setDataNascimento(dto.dataNascimento());
        perfil.setEndereco(dto.endereco());
        return ClientePerfilResponseDTO.from(clientePerfilRepository.save(perfil));
    }

    @Transactional(readOnly = true)
    public ClientePerfilResponseDTO meuPerfil(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return buscarPorId(usuario.getId());
    }

    @Transactional
    public ClientePerfilResponseDTO atualizarMeuPerfil(String email, ClientePerfilDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return atualizarPerfil(usuario.getId(), dto);
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> historicoCompras(Long clienteId) {
        return vendaRepository.findByClienteIdOrderByDataVendaDesc(clienteId).stream()
                .map(VendaResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> meuHistorico(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return historicoCompras(usuario.getId());
    }
}
