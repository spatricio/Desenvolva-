package compra.demo.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class Produto {
    @Positive(message = "id deve ser maior que zero")
    private int id;

    @NotBlank(message = "nome e obrigatorio")
    private String nome;

    @Positive(message = "preco deve ser maior que zero")
    private double preco;

    @Min(value = 1, message = "quantidade deve ser no minimo 1")
    private int quantidade;

    public Produto() {
    }

    public Produto(int id, String nome, double preco, int quantidade) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.quantidade = quantidade;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public double getPreco() {
        return preco;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public double getTotal() {
        return preco * quantidade;
    }
}
