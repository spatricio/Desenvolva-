package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.EntradaEstoqueDTO;
import drogsystem.drogsystem.dto.LoteResponseDTO;
import drogsystem.drogsystem.dto.MovimentacaoResponseDTO;
import drogsystem.drogsystem.dto.SaidaEstoqueDTO;
import drogsystem.drogsystem.entity.Lote;
import drogsystem.drogsystem.entity.MovimentacaoEstoque;
import drogsystem.drogsystem.entity.Produto;
import drogsystem.drogsystem.enums.TipoMovimentacao;
import drogsystem.drogsystem.event.EstoqueReabastecidoEvent;
import drogsystem.drogsystem.repository.LoteRepository;
import drogsystem.drogsystem.repository.MovimentacaoEstoqueRepository;
import drogsystem.drogsystem.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;
    private final MovimentacaoEstoqueRepository movimentacaoRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public MovimentacaoResponseDTO registrarEntrada(EntradaEstoqueDTO dto, String emailUsuario) {
        Produto produto = buscarProdutoAtivo(dto.produtoId());

        Lote lote = loteRepository.findByProdutoAndNumeroLote(produto, dto.numeroLote())
                .map(l -> {
                    l.setQuantidadeAtual(l.getQuantidadeAtual() + dto.quantidade());
                    return loteRepository.save(l);
                })
                .orElseGet(() -> loteRepository.save(Lote.builder()
                        .produto(produto)
                        .numeroLote(dto.numeroLote())
                        .dataValidade(dto.dataValidade())
                        .quantidadeInicial(dto.quantidade())
                        .quantidadeAtual(dto.quantidade())
                        .build()));

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + dto.quantidade());
        produtoRepository.save(produto);

        eventPublisher.publishEvent(
                new EstoqueReabastecidoEvent(produto.getId(), produto.getNome(), produto.getQuantidadeEstoque()));

        MovimentacaoEstoque mov = movimentacaoRepository.save(MovimentacaoEstoque.builder()
                .produto(produto)
                .lote(lote)
                .tipo(TipoMovimentacao.ENTRADA)
                .quantidade(dto.quantidade())
                .motivo(dto.motivo())
                .usuarioResponsavel(emailUsuario)
                .build());
        return MovimentacaoResponseDTO.from(mov);
    }

    @Transactional
    public MovimentacaoResponseDTO registrarSaida(SaidaEstoqueDTO dto, String emailUsuario) {
        Produto produto = buscarProdutoAtivo(dto.produtoId());

        if (produto.getQuantidadeEstoque() < dto.quantidade()) {
            throw new IllegalArgumentException(
                    "Estoque insuficiente. Disponível: " + produto.getQuantidadeEstoque());
        }

        produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - dto.quantidade());
        produtoRepository.save(produto);

        MovimentacaoEstoque mov = movimentacaoRepository.save(MovimentacaoEstoque.builder()
                .produto(produto)
                .tipo(TipoMovimentacao.SAIDA)
                .quantidade(dto.quantidade())
                .motivo(dto.motivo())
                .usuarioResponsavel(emailUsuario)
                .build());
        return MovimentacaoResponseDTO.from(mov);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoResponseDTO> listarMovimentacoes(Long produtoId) {
        return movimentacaoRepository.findByProdutoIdOrderByDataMovimentacaoDesc(produtoId)
                .stream()
                .map(MovimentacaoResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarLotesPorProduto(Long produtoId) {
        Produto produto = buscarProdutoAtivo(produtoId);
        return loteRepository.findByProduto(produto).stream()
                .map(LoteResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarLotesVencidos() {
        return loteRepository.findByDataValidadeBefore(LocalDate.now()).stream()
                .map(LoteResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LoteResponseDTO> listarLotesAVencer(int dias) {
        LocalDate hoje = LocalDate.now();
        return loteRepository.findByDataValidadeBetween(hoje, hoje.plusDays(dias)).stream()
                .map(LoteResponseDTO::from)
                .toList();
    }

    private Produto buscarProdutoAtivo(Long produtoId) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException("Produto inativo.");
        }
        return produto;
    }
}
