package drogsystem.drogsystem.controller;

import drogsystem.drogsystem.dto.ProdutoCadastroDTO;
import drogsystem.drogsystem.dto.ProdutoResponseDTO;
import drogsystem.drogsystem.enums.CategoriaProduto;
import drogsystem.drogsystem.service.ProdutoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<ProdutoResponseDTO> cadastrar(
            @Valid @RequestBody ProdutoCadastroDTO dto,
            UriComponentsBuilder uriBuilder) {
        ProdutoResponseDTO criado = produtoService.cadastrar(dto);
        var uri = uriBuilder.path("/api/produtos/{id}").buildAndExpand(criado.id()).toUri();
        return ResponseEntity.created(uri).body(criado);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<List<ProdutoResponseDTO>> listar() {
        return ResponseEntity.ok(produtoService.listar());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<ProdutoResponseDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(produtoService.buscarPorId(id));
    }

    @GetMapping("/categoria/{categoria}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO', 'ATENDENTE')")
    public ResponseEntity<List<ProdutoResponseDTO>> buscarPorCategoria(
            @PathVariable CategoriaProduto categoria) {
        return ResponseEntity.ok(produtoService.buscarPorCategoria(categoria));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<ProdutoResponseDTO> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProdutoCadastroDTO dto) {
        return ResponseEntity.ok(produtoService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        produtoService.desativar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/estoque-baixo")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'FARMACEUTICO')")
    public ResponseEntity<List<ProdutoResponseDTO>> listarAbaixoDoEstoqueMinimo() {
        return ResponseEntity.ok(produtoService.listarAbaixoDoEstoqueMinimo());
    }
}
