package compra.demo.service;

import compra.demo.model.Produto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarrinhoService {
    private final List<Produto> itens = new ArrayList<>();

    public void adicionarProduto(Produto p) {
        for (Produto item : itens) {
            if (item.getId() == p.getId()) {
                item.setQuantidade(item.getQuantidade() + p.getQuantidade());
                return;
            }
        }
        itens.add(p);
    }

    public void removerProduto(int id) {
        itens.removeIf(item -> item.getId() == id);
    }

    public List<Produto> listarItens() {
        return itens;
    }

    public Optional<Produto> buscarPorId(int id) {
        return itens.stream().filter(item -> item.getId() == id).findFirst();
    }

    public double calcularTotal() {
        return itens.stream().mapToDouble(Produto::getTotal).sum();
    }
}
