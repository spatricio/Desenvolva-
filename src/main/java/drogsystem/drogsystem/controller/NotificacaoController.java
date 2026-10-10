package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.NotificacaoResponseDTO;
import drogsystem.drogsystem.service.ReservaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notificacoes")
@RequiredArgsConstructor
public class NotificacaoController {

    private final ReservaService reservaService;

    @GetMapping
    public ResponseEntity<List<NotificacaoResponseDTO>> listar(Authentication authentication) {
        return ResponseEntity.ok(reservaService.listarNotificacoes(authentication.getName()));
    }

    @PostMapping("/{id}/marcar-lida")
    public ResponseEntity<Void> marcarLida(
            @PathVariable Long id,
            Authentication authentication) {
        reservaService.marcarLida(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/marcar-todas-lidas")
    public ResponseEntity<Map<String, Long>> marcarTodasLidas(Authentication authentication) {
        return ResponseEntity.ok(reservaService.marcarTodasLidas(authentication.getName()));
    }
}
