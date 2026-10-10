package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.EntradaEstoqueDTO;
import drogsystem.drogsystem.dto.LoteResponseDTO;
import drogsystem.drogsystem.dto.MovimentacaoResponseDTO;
import drogsystem.drogsystem.dto.SaidaEstoqueDTO;
import drogsystem.drogsystem.service.EstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estoque")
@RequiredArgsConstructor
public class EstoqueController {

    private final EstoqueService estoqueService;

    @PostMapping("/entrada")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<MovimentacaoResponseDTO> registrarEntrada(
            @Valid @RequestBody EntradaEstoqueDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(estoqueService.registrarEntrada(dto, authentication.getName()));
    }

    @PostMapping("/saida")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<MovimentacaoResponseDTO> registrarSaida(
            @Valid @RequestBody SaidaEstoqueDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(estoqueService.registrarSaida(dto, authentication.getName()));
    }

    @GetMapping("/movimentacoes/{produtoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<MovimentacaoResponseDTO>> listarMovimentacoes(
            @PathVariable Long produtoId) {
        return ResponseEntity.ok(estoqueService.listarMovimentacoes(produtoId));
    }

    @GetMapping("/lotes/{produtoId}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<List<LoteResponseDTO>> listarLotesPorProduto(
            @PathVariable Long produtoId) {
        return ResponseEntity.ok(estoqueService.listarLotesPorProduto(produtoId));
    }

    @GetMapping("/lotes/vencidos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<LoteResponseDTO>> listarLotesVencidos() {
        return ResponseEntity.ok(estoqueService.listarLotesVencidos());
    }

    @GetMapping("/lotes/a-vencer")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<LoteResponseDTO>> listarLotesAVencer(
            @RequestParam(defaultValue = "30") int dias) {
        return ResponseEntity.ok(estoqueService.listarLotesAVencer(dias));
    }
}
