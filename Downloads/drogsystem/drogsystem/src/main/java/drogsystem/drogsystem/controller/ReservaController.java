package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.ReservaCadastroDTO;
import drogsystem.drogsystem.dto.ReservaResponseDTO;
import drogsystem.drogsystem.service.ReservaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
@RequiredArgsConstructor
public class ReservaController {

    private final ReservaService reservaService;

    @PostMapping
    public ResponseEntity<ReservaResponseDTO> fazer(
            @Valid @RequestBody ReservaCadastroDTO dto,
            Authentication authentication,
            UriComponentsBuilder uriBuilder) {
        ReservaResponseDTO criada = reservaService.fazer(dto, authentication.getName());
        var uri = uriBuilder.path("/api/reservas/{id}").buildAndExpand(criada.id()).toUri();
        return ResponseEntity.created(uri).body(criada);
    }

    @GetMapping("/minhas")
    public ResponseEntity<List<ReservaResponseDTO>> listarMinhas(Authentication authentication) {
        return ResponseEntity.ok(reservaService.listarMinhas(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservaResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(reservaService.buscarPorId(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<ReservaResponseDTO>> listar() {
        return ResponseEntity.ok(reservaService.listar());
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<ReservaResponseDTO> confirmar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(reservaService.confirmar(id, authentication.getName()));
    }

    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ReservaResponseDTO> cancelar(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(reservaService.cancelar(id, authentication));
    }
}
