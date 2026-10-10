package drogsystem.drogsystem.event;

public record EstoqueReabastecidoEvent(Long produtoId, String nomeProduto, int quantidadeDisponivel) {
}
