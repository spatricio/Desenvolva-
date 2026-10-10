package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.PromocaoCadastroDTO;
import drogsystem.drogsystem.dto.PromocaoResponseDTO;
import drogsystem.drogsystem.service.PromocaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/promocoes")
@RequiredArgsConstructor
public class PromocaoController {

    private final PromocaoService promocaoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<PromocaoResponseDTO> cadastrar(
            @Valid @RequestBody PromocaoCadastroDTO dto,
            UriComponentsBuilder uriBuilder) {
        PromocaoResponseDTO criada = promocaoService.cadastrar(dto);
        var uri = uriBuilder.path("/api/promocoes/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<PromocaoResponseDTO>> listar() {
        return ResponseEntity.ok(promocaoService.listar());
    }

    @GetMapping("/vigentes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<List<PromocaoResponseDTO>> listarVigentes() {
        return ResponseEntity.ok(promocaoService.listarVigentes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<PromocaoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(promocaoService.buscarPorId(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        promocaoService.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
