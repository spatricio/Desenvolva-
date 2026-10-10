package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.*;
import drogsystem.drogsystem.entity.ItemVenda;
import drogsystem.drogsystem.entity.Venda;
import drogsystem.drogsystem.enums.StatusVenda;
import drogsystem.drogsystem.repository.LoteRepository;
import drogsystem.drogsystem.repository.ProdutoRepository;
import drogsystem.drogsystem.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final VendaRepository vendaRepository;
    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;

    @Transactional(readOnly = true)
    public RelatorioVendasDTO gerarRelatorioVendas(LocalDate dataInicio, LocalDate dataFim) {
        LocalDateTime inicio = dataInicio.atStartOfDay();
        LocalDateTime fim = dataFim.atTime(23, 59, 59);

        List<Venda> todas = vendaRepository.findByDataVendaBetweenOrderByDataVendaDesc(inicio, fim);

        List<Venda> concluidas = todas.stream()
                .filter(v -> v.getStatus() == StatusVenda.CONCLUIDA)
                .toList();

        long totalCanceladas = todas.stream()
                .filter(v -> v.getStatus() == StatusVenda.CANCELADA)
                .count();

        BigDecimal receitaTotal = concluidas.stream()
                .map(Venda::getTotalFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal ticketMedio = concluidas.isEmpty() ? BigDecimal.ZERO
                : receitaTotal.divide(BigDecimal.valueOf(concluidas.size()), 2, RoundingMode.HALF_UP);

        List<ItemVendasPorDiaDTO> vendasPorDia = concluidas.stream()
                .collect(Collectors.groupingBy(v -> v.getDataVenda().toLocalDate()))
                .entrySet().stream()
                .map(e -> new ItemVendasPorDiaDTO(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().map(Venda::getTotalFinal).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparing(ItemVendasPorDiaDTO::data))
                .toList();

        List<VendasPorFormaPagamentoDTO> vendasPorFormaPagamento = concluidas.stream()
                .collect(Collectors.groupingBy(Venda::getFormaPagamento))
                .entrySet().stream()
                .map(e -> new VendasPorFormaPagamentoDTO(
                        e.getKey(),
                        e.getValue().size(),
                        e.getValue().stream().map(Venda::getTotalFinal).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();

        Map<Long, List<ItemVenda>> itensPorProduto = concluidas.stream()
                .flatMap(v -> v.getItens().stream())
                .collect(Collectors.groupingBy(i -> i.getProduto().getId()));

        List<ProdutoMaisVendidoDTO> topProdutos = itensPorProduto.entrySet().stream()
                .map(e -> {
                    List<ItemVenda> itens = e.getValue();
                    return new ProdutoMaisVendidoDTO(
                            e.getKey(),
                            itens.get(0).getProduto().getNome(),
                            itens.stream().mapToLong(ItemVenda::getQuantidade).sum(),
                            itens.stream().map(ItemVenda::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add));
                })
                .sorted(Comparator.comparingLong(ProdutoMaisVendidoDTO::quantidadeVendida).reversed())
                .limit(10)
                .toList();

        return new RelatorioVendasDTO(
                dataInicio, dataFim,
                concluidas.size(), totalCanceladas,
                receitaTotal, ticketMedio,
                vendasPorDia, vendasPorFormaPagamento, topProdutos);
    }

    @Transactional(readOnly = true)
    public RelatorioEstoqueDTO gerarRelatorioEstoque() {
        List<ProdutoResponseDTO> abaixoMinimo = produtoRepository.findAbaixoDoEstoqueMinimo().stream()
                .map(ProdutoResponseDTO::from)
                .toList();

        List<LoteResponseDTO> vencidos = loteRepository.findByDataValidadeBefore(LocalDate.now()).stream()
                .map(LoteResponseDTO::from)
                .toList();

        List<LoteResponseDTO> aVencer = loteRepository
                .findByDataValidadeBetween(LocalDate.now(), LocalDate.now().plusDays(30)).stream()
                .map(LoteResponseDTO::from)
                .toList();

        int totalAtivos = produtoRepository.findByAtivoTrue().size();

        return new RelatorioEstoqueDTO(
                totalAtivos,
                abaixoMinimo.size(),
                vencidos.size(),
                aVencer.size(),
                abaixoMinimo,
                vencidos,
                aVencer);
    }
}
