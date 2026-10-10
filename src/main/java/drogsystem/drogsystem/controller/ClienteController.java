package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.ClientePerfilDTO;
import drogsystem.drogsystem.dto.ClientePerfilResponseDTO;
import drogsystem.drogsystem.dto.VendaResponseDTO;
import drogsystem.drogsystem.service.ClienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<ClientePerfilResponseDTO>> listar() {
        return ResponseEntity.ok(clienteService.listarClientes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<ClientePerfilResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

    @GetMapping("/{id}/historico")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<VendaResponseDTO>> historico(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.historicoCompras(id));
    }

    @GetMapping("/meu-perfil")
    public ResponseEntity<ClientePerfilResponseDTO> meuPerfil(Authentication authentication) {
        return ResponseEntity.ok(clienteService.meuPerfil(authentication.getName()));
    }

    @PutMapping("/meu-perfil")
    public ResponseEntity<ClientePerfilResponseDTO> atualizarMeuPerfil(
            @RequestBody ClientePerfilDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(clienteService.atualizarMeuPerfil(authentication.getName(), dto));
    }

    @GetMapping("/meu-perfil/historico")
    public ResponseEntity<List<VendaResponseDTO>> meuHistorico(Authentication authentication) {
        return ResponseEntity.ok(clienteService.meuHistorico(authentication.getName()));
    }
}
