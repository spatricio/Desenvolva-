package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.VendaCadastroDTO;
import drogsystem.drogsystem.dto.VendaResponseDTO;
import drogsystem.drogsystem.entity.*;
import drogsystem.drogsystem.enums.FormaPagamento;
import drogsystem.drogsystem.enums.StatusVenda;
import drogsystem.drogsystem.enums.TipoDesconto;
import drogsystem.drogsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VendaService {

    private final VendaRepository vendaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;
    private final PromocaoRepository promocaoRepository;
    private final FidelidadeService fidelidadeService;

    @Transactional
    public VendaResponseDTO registrar(VendaCadastroDTO dto, String emailAtendente) {
        Usuario atendente = usuarioRepository.findByEmail(emailAtendente)
                .orElseThrow(() -> new IllegalArgumentException("Atendente não encontrado."));

        Usuario cliente = null;
        if (dto.clienteId() != null) {
            cliente = usuarioRepository.findById(dto.clienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado."));
        }

        List<ItemVenda> itens = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (var itemDTO : dto.itens()) {
            Produto produto = produtoRepository.findById(itemDTO.produtoId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Produto não encontrado: id=" + itemDTO.produtoId()));
            if (!produto.isAtivo()) {
                throw new IllegalArgumentException("Produto inativo: " + produto.getNome());
            }
            if (produto.getQuantidadeEstoque() < itemDTO.quantidade()) {
                throw new IllegalArgumentException(
                        "Estoque insuficiente para: " + produto.getNome()
                                + ". Disponível: " + produto.getQuantidadeEstoque());
            }
            BigDecimal itemSubtotal = produto.getPreco()
                    .multiply(BigDecimal.valueOf(itemDTO.quantidade()));
            itens.add(ItemVenda.builder()
                    .produto(produto)
                    .quantidade(itemDTO.quantidade())
                    .precoUnitario(produto.getPreco())
                    .subtotal(itemSubtotal)
                    .build());
            subtotal = subtotal.add(itemSubtotal);
        }

        Promocao promocao = null;
        BigDecimal desconto = BigDecimal.ZERO;
        if (dto.promocaoId() != null) {
            promocao = promocaoRepository.findById(dto.promocaoId())
                    .orElseThrow(() -> new IllegalArgumentException("Promoção não encontrada."));
            if (!promocao.isVigente()) {
                throw new IllegalArgumentException("Promoção não está vigente.");
            }
            desconto = calcularDesconto(subtotal, promocao);
        }

        int pontosParaResgatar = dto.pontosParaResgatar() != null ? dto.pontosParaResgatar() : 0;
        if (pontosParaResgatar > 0) {
            if (cliente == null) {
                throw new IllegalArgumentException("Para usar pontos, informe o cliente na venda.");
            }
            desconto = desconto.add(fidelidadeService.calcularDescontoPorPontos(cliente.getId(), pontosParaResgatar));
        }

        BigDecimal totalFinal = subtotal.subtract(desconto).max(BigDecimal.ZERO);
        int pontosGerados = cliente != null ? totalFinal.intValue() : 0;

        BigDecimal valorPago = dto.valorPago();
        BigDecimal troco = BigDecimal.ZERO;

        if (dto.formaPagamento() == FormaPagamento.DINHEIRO) {
            if (valorPago.compareTo(totalFinal) < 0) {
                throw new IllegalArgumentException(
                        "Valor pago insuficiente. Total: R$ " + totalFinal);
            }
            troco = valorPago.subtract(totalFinal);
        } else {
            // para pagamentos eletrônicos, valor pago é o total final
            valorPago = totalFinal;
        }

        Venda venda = Venda.builder()
                .cliente(cliente)
                .atendente(atendente)
                .promocao(promocao)
                .subtotal(subtotal)
                .desconto(desconto)
                .totalFinal(totalFinal)
                .formaPagamento(dto.formaPagamento())
                .valorPago(valorPago)
                .troco(troco)
                .status(StatusVenda.CONCLUIDA)
                .observacoes(dto.observacoes())
                .pontosGerados(pontosGerados)
                .pontosResgatados(pontosParaResgatar)
                .build();

        venda = vendaRepository.save(venda);

        // cupom gerado após persistência para usar o ID
        venda.setNumeroCupom(String.format("CUPOM-%s-%06d",
                venda.getDataVenda().format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                venda.getId()));

        for (ItemVenda item : itens) {
            item.setVenda(venda);
            venda.getItens().add(item);
        }

        venda = vendaRepository.save(venda);

        // baixar estoque de cada item
        for (ItemVenda item : itens) {
            Produto produto = item.getProduto();
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() - item.getQuantidade());
            produtoRepository.save(produto);
        }

        if (cliente != null) {
            fidelidadeService.registrarCompra(cliente.getId(), totalFinal, pontosGerados, pontosParaResgatar);
        }

        return VendaResponseDTO.from(venda);
    }

    @Transactional
    public VendaResponseDTO cancelar(Long id) {
        Venda venda = buscarEntidade(id);
        if (venda.getStatus() == StatusVenda.CANCELADA) {
            throw new IllegalArgumentException("Venda já cancelada.");
        }
        // devolver estoque
        for (ItemVenda item : venda.getItens()) {
            Produto produto = item.getProduto();
            produto.setQuantidadeEstoque(produto.getQuantidadeEstoque() + item.getQuantidade());
            produtoRepository.save(produto);
        }
        if (venda.getCliente() != null) {
            fidelidadeService.cancelarCompra(
                    venda.getCliente().getId(),
                    venda.getPontosGerados(),
                    venda.getPontosResgatados());
        }
        venda.setStatus(StatusVenda.CANCELADA);
        return VendaResponseDTO.from(vendaRepository.save(venda));
    }

    @Transactional(readOnly = true)
    public VendaResponseDTO buscarPorId(Long id) {
        return VendaResponseDTO.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<VendaResponseDTO> listar() {
        return vendaRepository.findAll().stream()
                .map(VendaResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public String gerarCupomTexto(Long id) {
        Venda v = buscarEntidade(id);
        var sb = new StringBuilder();
        String linha = "=".repeat(42);
        String sep = "-".repeat(42);

        sb.append(linha).append("\n");
        sb.append("        DROGSYSTEM - FARMÁCIA\n");
        sb.append(linha).append("\n");
        sb.append(String.format("Cupom  : %s%n", v.getNumeroCupom()));
        sb.append(String.format("Data   : %s%n",
                v.getDataVenda().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        sb.append(String.format("Atend. : %s%n", v.getAtendente().getNome()));
        sb.append(String.format("Cliente: %s%n",
                v.getCliente() != null ? v.getCliente().getNome() : "Consumidor Final"));
        sb.append(sep).append("\n");
        sb.append("PRODUTOS:\n");

        for (ItemVenda item : v.getItens()) {
            sb.append(String.format("  %dx %-28s R$ %7.2f%n",
                    item.getQuantidade(),
                    item.getProduto().getNome(),
                    item.getSubtotal()));
        }

        sb.append(sep).append("\n");
        sb.append(String.format("%-30s R$ %7.2f%n", "Subtotal:", v.getSubtotal()));
        if (v.getDesconto().compareTo(BigDecimal.ZERO) > 0) {
            sb.append(String.format("%-30s R$ %7.2f%n", "Desconto:", v.getDesconto()));
        }
        sb.append(String.format("%-30s R$ %7.2f%n", "TOTAL:", v.getTotalFinal()));
        sb.append(sep).append("\n");
        sb.append(String.format("Pagamento: %s%n", v.getFormaPagamento().name().replace("_", " ")));
        sb.append(String.format("%-30s R$ %7.2f%n", "Valor Pago:", v.getValorPago()));
        if (v.getFormaPagamento() == FormaPagamento.DINHEIRO) {
            sb.append(String.format("%-30s R$ %7.2f%n", "Troco:", v.getTroco()));
        }
        if (v.getCliente() != null && v.getPontosGerados() > 0) {
            sb.append(sep).append("\n");
            sb.append(String.format("%-30s %7d pts%n", "Pontos Ganhos:", v.getPontosGerados()));
            if (v.getPontosResgatados() > 0) {
                sb.append(String.format("%-30s %7d pts%n", "Pontos Usados:", v.getPontosResgatados()));
            }
        }
        sb.append(linha).append("\n");
        sb.append("      OBRIGADO PELA COMPRA!\n");
        sb.append(linha).append("\n");

        return sb.toString();
    }

    private Venda buscarEntidade(Long id) {
        return vendaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venda não encontrada."));
    }

    private BigDecimal calcularDesconto(BigDecimal subtotal, Promocao promocao) {
        if (promocao.getTipoDesconto() == TipoDesconto.PERCENTUAL) {
            return subtotal
                    .multiply(promocao.getValorDesconto())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        }
        return promocao.getValorDesconto().min(subtotal);
    }
}
