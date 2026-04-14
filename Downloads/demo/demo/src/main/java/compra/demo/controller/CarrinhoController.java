package compra.demo.controller;

import compra.demo.model.Produto;
import compra.demo.service.CarrinhoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/carrinho")
@Validated
public class CarrinhoController {

    private final CarrinhoService service;

    public CarrinhoController(CarrinhoService service) {
        this.service = service;
    }

    @PostMapping("/add")
    public void addProduto(@Valid @RequestBody Produto p) {
        service.adicionarProduto(p);
    }

    @DeleteMapping("/{id}")
    public void removerProduto(@PathVariable @Positive(message = "id deve ser maior que zero") int id) {
        service.removerProduto(id);
    }

    @GetMapping
    public List<Produto> listar() {
        return service.listarItens();
    }

    @GetMapping("/produtos")
    public List<Produto> listarProdutos() {
        return service.listarItens();
    }

    @GetMapping("/produtos/{id}")
    public ResponseEntity<Produto> buscarProduto(@PathVariable @Positive(message = "id deve ser maior que zero") int id) {
        return service.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/produtos")
    public ResponseEntity<Produto> criarProduto(@Valid @RequestBody Produto p) {
        service.adicionarProduto(p);
        return ResponseEntity.status(HttpStatus.CREATED).body(p);
    }

    @DeleteMapping("/produtos/{id}")
    public ResponseEntity<Void> removerProdutoRest(@PathVariable @Positive(message = "id deve ser maior que zero") int id) {
        if (service.buscarPorId(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        service.removerProduto(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/total")
    public double total() {
        return service.calcularTotal();
    }

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "mensagem", "Carrinho online",
                "quantidadeItens", service.listarItens().size(),
                "total", service.calcularTotal()
        );
    }
}
