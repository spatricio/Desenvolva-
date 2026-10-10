package drogsystem.drogsystem.config;

import drogsystem.drogsystem.entity.ClientePerfil;
import drogsystem.drogsystem.entity.ItemVenda;
import drogsystem.drogsystem.entity.Lote;
import drogsystem.drogsystem.entity.MovimentacaoEstoque;
import drogsystem.drogsystem.entity.Notificacao;
import drogsystem.drogsystem.entity.Promocao;
import drogsystem.drogsystem.entity.Produto;
import drogsystem.drogsystem.entity.Reserva;
import drogsystem.drogsystem.entity.Usuario;
import drogsystem.drogsystem.entity.Venda;
import drogsystem.drogsystem.enums.CategoriaProduto;
import drogsystem.drogsystem.enums.FormaPagamento;
import drogsystem.drogsystem.enums.NivelFidelidade;
import drogsystem.drogsystem.enums.StatusReserva;
import drogsystem.drogsystem.enums.StatusVenda;
import drogsystem.drogsystem.enums.TipoDesconto;
import drogsystem.drogsystem.enums.TipoPerfil;
import drogsystem.drogsystem.enums.TipoMovimentacao;
import drogsystem.drogsystem.repository.ClientePerfilRepository;
import drogsystem.drogsystem.repository.LoteRepository;
import drogsystem.drogsystem.repository.MovimentacaoEstoqueRepository;
import drogsystem.drogsystem.repository.NotificacaoRepository;
import drogsystem.drogsystem.repository.PromocaoRepository;
import drogsystem.drogsystem.repository.ProdutoRepository;
import drogsystem.drogsystem.repository.ReservaRepository;
import drogsystem.drogsystem.repository.UsuarioRepository;
import drogsystem.drogsystem.repository.VendaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClientePerfilRepository clientePerfilRepository;
    private final ProdutoRepository produtoRepository;
    private final LoteRepository loteRepository;
    private final PromocaoRepository promocaoRepository;
    private final ReservaRepository reservaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final VendaRepository vendaRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
    Usuario admin = ensureUser("Administrador", "admin@drogsystem.com", "admin123", TipoPerfil.ADMINISTRADOR);
    Usuario farmaceutico = ensureUser("Fernanda Souza", "farmaceutico@drogsystem.com", "farmacia123", TipoPerfil.FARMACEUTICO);
    Usuario atendente = ensureUser("Carlos Lima", "atendente@drogsystem.com", "atendimento123", TipoPerfil.ATENDENTE);
    Usuario cliente = ensureUser("Mariana Costa", "cliente@drogsystem.com", "cliente123", TipoPerfil.CLIENTE);

    ensureClientProfile(cliente);

    Produto dipirona = ensureProduct("789100000001", "Dipirona 500mg", "Analgesico de uso geral.", CategoriaProduto.GENERICO,
        "CX", 80, 20, new BigDecimal("12.90"));
    Produto protetorSolar = ensureProduct("789100000002", "Protetor Solar FPS 60", "Uso diario com alta protecao.", CategoriaProduto.COSMETICO,
        "UN", 12, 15, new BigDecimal("54.90"));
    Produto vitaminaC = ensureProduct("789100000003", "Vitamina C 1g", "Suplemento efervescente com 10 comprimidos.", CategoriaProduto.SUPLEMENTO,
        "TB", 35, 10, new BigDecimal("29.90"));

    ensureLot(dipirona, "L-DIP-2026-01", LocalDate.now().plusMonths(10), 80);
    ensureLot(protetorSolar, "L-SOL-2026-02", LocalDate.now().plusMonths(4), 12);
    ensureLot(vitaminaC, "L-VIT-2026-03", LocalDate.now().plusMonths(8), 35);

    ensureMovement(dipirona, "L-DIP-2026-01", TipoMovimentacao.ENTRADA, 80, "Carga inicial", admin.getEmail());
    ensureMovement(protetorSolar, "L-SOL-2026-02", TipoMovimentacao.ENTRADA, 12, "Carga inicial", farmaceutico.getEmail());
    ensureMovement(vitaminaC, "L-VIT-2026-03", TipoMovimentacao.ENTRADA, 35, "Carga inicial", farmaceutico.getEmail());

    Promocao promocao = ensurePromotion();
    Reserva reserva = ensureReservation(cliente, protetorSolar);
    ensureNotification(cliente, reserva);
    ensureSale(cliente, atendente, dipirona, vitaminaC, promocao);

        log.info("==============================================");
    log.info("  Dados de demonstração prontos para uso.");
    log.info("  Admin         : admin@drogsystem.com / admin123");
    log.info("  Farmaceutico  : farmaceutico@drogsystem.com / farmacia123");
    log.info("  Atendente     : atendente@drogsystem.com / atendimento123");
    log.info("  Cliente       : cliente@drogsystem.com / cliente123");
        log.info("==============================================");
    }

    private Usuario ensureUser(String nome, String email, String senha, TipoPerfil perfil) {
    return usuarioRepository.findByEmail(email)
        .orElseGet(() -> usuarioRepository.save(Usuario.builder()
            .nome(nome)
            .email(email)
            .senha(passwordEncoder.encode(senha))
            .perfil(perfil)
            .ativo(true)
            .build()));
    }

    private void ensureClientProfile(Usuario cliente) {
    if (clientePerfilRepository.existsById(cliente.getId())) {
        return;
    }
    clientePerfilRepository.save(ClientePerfil.builder()
        .usuario(cliente)
        .cpf("12345678900")
        .telefone("11999998888")
        .dataNascimento(LocalDate.of(1993, 5, 18))
        .endereco("Rua das Flores, 120 - Centro")
        .pontosAcumulados(140)
        .totalGasto(new BigDecimal("1240.50"))
        .nivel(NivelFidelidade.OURO)
        .dataUltimaCompra(LocalDateTime.now().minusDays(2))
        .build());
    }

    private Produto ensureProduct(String codigoBarras, String nome, String descricao, CategoriaProduto categoria,
                  String unidadeMedida, int quantidadeEstoque, int estoqueMinimo, BigDecimal preco) {
    return produtoRepository.findByCodigoBarras(codigoBarras)
        .orElseGet(() -> produtoRepository.save(Produto.builder()
            .nome(nome)
            .descricao(descricao)
            .codigoBarras(codigoBarras)
            .categoria(categoria)
            .unidadeMedida(unidadeMedida)
            .quantidadeEstoque(quantidadeEstoque)
            .estoqueMinimo(estoqueMinimo)
            .preco(preco)
            .ativo(true)
            .build()));
    }

    private void ensureLot(Produto produto, String numeroLote, LocalDate dataValidade, int quantidade) {
    if (loteRepository.findByProdutoAndNumeroLote(produto, numeroLote).isPresent()) {
        return;
    }
    loteRepository.save(Lote.builder()
        .produto(produto)
        .numeroLote(numeroLote)
        .dataValidade(dataValidade)
        .quantidadeInicial(quantidade)
        .quantidadeAtual(quantidade)
        .build());
    }

    private void ensureMovement(Produto produto, String numeroLote, TipoMovimentacao tipo, int quantidade, String motivo,
                String usuarioResponsavel) {
    boolean exists = movimentacaoEstoqueRepository.findByProdutoIdOrderByDataMovimentacaoDesc(produto.getId()).stream()
        .anyMatch(item -> item.getTipo() == tipo
            && item.getQuantidade() == quantidade
            && motivo.equals(item.getMotivo())
            && usuarioResponsavel.equals(item.getUsuarioResponsavel()));
    if (exists) {
        return;
    }
    Lote lote = loteRepository.findByProdutoAndNumeroLote(produto, numeroLote).orElse(null);
    movimentacaoEstoqueRepository.save(MovimentacaoEstoque.builder()
        .produto(produto)
        .lote(lote)
        .tipo(tipo)
        .quantidade(quantidade)
        .motivo(motivo)
        .usuarioResponsavel(usuarioResponsavel)
        .build());
    }

    private Promocao ensurePromotion() {
    return promocaoRepository.findAll().stream()
        .filter(item -> "Semana da Saude".equals(item.getNome()))
        .findFirst()
        .orElseGet(() -> promocaoRepository.save(Promocao.builder()
            .nome("Semana da Saude")
            .descricao("10% de desconto em itens selecionados.")
            .tipoDesconto(TipoDesconto.PERCENTUAL)
            .valorDesconto(new BigDecimal("10.00"))
            .dataInicio(LocalDate.now().minusDays(2))
            .dataFim(LocalDate.now().plusDays(12))
            .ativa(true)
            .build()));
    }

    private Reserva ensureReservation(Usuario cliente, Produto produto) {
    return reservaRepository.findByClienteIdOrderByDataReservaDesc(cliente.getId()).stream()
        .filter(item -> item.getProduto().getId().equals(produto.getId()))
        .findFirst()
        .orElseGet(() -> reservaRepository.save(Reserva.builder()
            .cliente(cliente)
            .produto(produto)
            .quantidade(2)
            .status(StatusReserva.DISPONIVEL)
            .observacoes("Reserva criada para validar o fluxo da interface.")
            .dataNotificacao(LocalDateTime.now().minusHours(4))
            .dataExpiracao(LocalDateTime.now().plusDays(3))
            .build()));
    }

    private void ensureNotification(Usuario cliente, Reserva reserva) {
    boolean exists = notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(cliente.getId()).stream()
        .anyMatch(item -> "Reserva liberada".equals(item.getTitulo()));
    if (exists) {
        return;
    }
    notificacaoRepository.save(Notificacao.builder()
        .usuario(cliente)
        .titulo("Reserva liberada")
        .mensagem("Sua reserva do produto está disponível para retirada.")
        .lida(false)
        .reserva(reserva)
        .build());
    }

    private void ensureSale(Usuario cliente, Usuario atendente, Produto principal, Produto adicional, Promocao promocao) {
    boolean exists = vendaRepository.findByClienteIdOrderByDataVendaDesc(cliente.getId()).stream()
        .anyMatch(item -> "CPM-000001".equals(item.getNumeroCupom()));
    if (exists) {
        return;
    }

    BigDecimal subtotal = principal.getPreco().multiply(BigDecimal.valueOf(2))
        .add(adicional.getPreco());
    BigDecimal desconto = subtotal.multiply(new BigDecimal("0.10"));
    BigDecimal totalFinal = subtotal.subtract(desconto);
    BigDecimal valorPago = new BigDecimal("100.00");

    Venda venda = Venda.builder()
        .cliente(cliente)
        .atendente(atendente)
        .promocao(promocao)
        .subtotal(subtotal)
        .desconto(desconto)
        .totalFinal(totalFinal)
        .formaPagamento(FormaPagamento.PIX)
        .valorPago(valorPago)
        .troco(valorPago.subtract(totalFinal))
        .status(StatusVenda.CONCLUIDA)
        .observacoes("Venda inicial para validar relatorios e cupom.")
        .numeroCupom("CPM-000001")
        .pontosGerados(12)
        .pontosResgatados(0)
        .build();

    ItemVenda itemPrincipal = ItemVenda.builder()
        .venda(venda)
        .produto(principal)
        .quantidade(2)
        .precoUnitario(principal.getPreco())
        .subtotal(principal.getPreco().multiply(BigDecimal.valueOf(2)))
        .build();
    ItemVenda itemAdicional = ItemVenda.builder()
        .venda(venda)
        .produto(adicional)
        .quantidade(1)
        .precoUnitario(adicional.getPreco())
        .subtotal(adicional.getPreco())
        .build();

    venda.setItens(List.of(itemPrincipal, itemAdicional));
    vendaRepository.save(venda);
    }
}
