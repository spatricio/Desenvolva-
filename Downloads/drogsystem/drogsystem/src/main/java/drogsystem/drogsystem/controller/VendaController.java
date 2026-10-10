package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.VendaCadastroDTO;
import drogsystem.drogsystem.dto.VendaResponseDTO;
import drogsystem.drogsystem.service.VendaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/vendas")
@RequiredArgsConstructor
public class VendaController {

    private final VendaService vendaService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<VendaResponseDTO> registrar(
            @Valid @RequestBody VendaCadastroDTO dto,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        VendaResponseDTO criada = vendaService.registrar(dto, authentication.getName());
        var uri = uriBuilder.path("/api/vendas/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<VendaResponseDTO>> listar() {
        return ResponseEntity.ok(vendaService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<VendaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.buscarPorId(id));
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<VendaResponseDTO> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.cancelar(id));
    }

    @GetMapping(value = "/{id}/cupom", produces = MediaType.TEXT_PLAIN_VALUE)
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<String> gerarCupom(@PathVariable Long id) {
        return ResponseEntity.ok(vendaService.gerarCupomTexto(id));
    }
}
