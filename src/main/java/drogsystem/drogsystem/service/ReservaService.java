package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.NotificacaoResponseDTO;
import drogsystem.drogsystem.dto.ReservaCadastroDTO;
import drogsystem.drogsystem.dto.ReservaResponseDTO;
import drogsystem.drogsystem.entity.*;
import drogsystem.drogsystem.enums.StatusReserva;
import drogsystem.drogsystem.event.EstoqueReabastecidoEvent;
import drogsystem.drogsystem.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservaService {

    private final ReservaRepository reservaRepository;
    private final NotificacaoRepository notificacaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProdutoRepository produtoRepository;

    @Transactional
    public ReservaResponseDTO fazer(ReservaCadastroDTO dto, String emailCliente) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        Produto produto = produtoRepository.findById(dto.produtoId())
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (!produto.isAtivo()) {
            throw new IllegalArgumentException("Produto inativo.");
        }

        // se já tem estoque suficiente, notifica imediatamente
        StatusReserva statusInicial = produto.getQuantidadeEstoque() >= dto.quantidade()
                ? StatusReserva.DISPONIVEL
                : StatusReserva.PENDENTE;

        LocalDateTime agora = LocalDateTime.now();
        Reserva reserva = Reserva.builder()
                .cliente(cliente)
                .produto(produto)
                .quantidade(dto.quantidade())
                .status(statusInicial)
                .observacoes(dto.observacoes())
                .dataNotificacao(statusInicial == StatusReserva.DISPONIVEL ? agora : null)
                .dataExpiracao(statusInicial == StatusReserva.DISPONIVEL ? agora.plusDays(3) : null)
                .build();

        reserva = reservaRepository.save(reserva);

        if (statusInicial == StatusReserva.DISPONIVEL) {
            criarNotificacao(cliente, produto.getNome(), reserva);
        }

        return ReservaResponseDTO.from(reserva);
    }

    @Transactional
    public ReservaResponseDTO confirmar(Long id, String emailUsuario) {
        Reserva reserva = buscarEntidade(id);
        verificarProprietario(reserva, emailUsuario, null);
        if (reserva.getStatus() != StatusReserva.DISPONIVEL) {
            throw new IllegalArgumentException(
                    "Reserva não está disponível para confirmação. Status: " + reserva.getStatus());
        }
        reserva.setStatus(StatusReserva.CONFIRMADA);
        return ReservaResponseDTO.from(reservaRepository.save(reserva));
    }

    @Transactional
    public ReservaResponseDTO cancelar(Long id, Authentication auth) {
        Reserva reserva = buscarEntidade(id);
        verificarProprietario(reserva, auth.getName(), auth);
        if (reserva.getStatus() == StatusReserva.CANCELADA || reserva.getStatus() == StatusReserva.CONFIRMADA) {
            throw new IllegalArgumentException(
                    "Não é possível cancelar uma reserva com status: " + reserva.getStatus());
        }
        reserva.setStatus(StatusReserva.CANCELADA);
        return ReservaResponseDTO.from(reservaRepository.save(reserva));
    }

    @Transactional(readOnly = true)
    public ReservaResponseDTO buscarPorId(Long id) {
        return ReservaResponseDTO.from(buscarEntidade(id));
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listar() {
        return reservaRepository.findAllByOrderByDataReservaDesc().stream()
                .map(ReservaResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReservaResponseDTO> listarMinhas(String emailCliente) {
        Usuario cliente = usuarioRepository.findByEmail(emailCliente)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return reservaRepository.findByClienteIdOrderByDataReservaDesc(cliente.getId()).stream()
                .map(ReservaResponseDTO::from)
                .toList();
    }

    // notificações

    @Transactional(readOnly = true)
    public List<NotificacaoResponseDTO> listarNotificacoes(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        return notificacaoRepository.findByUsuarioIdOrderByDataCriacaoDesc(usuario.getId()).stream()
                .map(NotificacaoResponseDTO::from)
                .toList();
    }

    @Transactional
    public void marcarLida(Long notificacaoId, String email) {
        Notificacao notificacao = notificacaoRepository.findById(notificacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Notificação não encontrada."));
        if (!notificacao.getUsuario().getEmail().equals(email)) {
            throw new AccessDeniedException("Sem permissão.");
        }
        notificacao.setLida(true);
        notificacaoRepository.save(notificacao);
    }

    @Transactional
    public Map<String, Long> marcarTodasLidas(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));
        List<Notificacao> naoLidas = notificacaoRepository.findByUsuarioIdAndLidaFalse(usuario.getId());
        naoLidas.forEach(n -> n.setLida(true));
        notificacaoRepository.saveAll(naoLidas);
        return Map.of("marcadas", (long) naoLidas.size());
    }

    // escuta entrada de estoque e notifica reservas pendentes (após commit da
    // transação de entrada)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onEstoqueReabastecido(EstoqueReabastecidoEvent event) {
        List<Reserva> pendentes = reservaRepository
                .findByProdutoIdAndStatus(event.produtoId(), StatusReserva.PENDENTE);

        int estoqueDisponivel = event.quantidadeDisponivel();
        LocalDateTime agora = LocalDateTime.now();

        for (Reserva reserva : pendentes) {
            if (estoqueDisponivel < reserva.getQuantidade()) {
                break;
            }
            reserva.setStatus(StatusReserva.DISPONIVEL);
            reserva.setDataNotificacao(agora);
            reserva.setDataExpiracao(agora.plusDays(3));
            reservaRepository.save(reserva);
            criarNotificacao(reserva.getCliente(), event.nomeProduto(), reserva);
            estoqueDisponivel -= reserva.getQuantidade();
        }
    }

    // expira reservas disponíveis cujo prazo venceu — executa a cada hora
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void expirarReservasVencidas() {
        reservaRepository
                .findByStatusAndDataExpiracaoBefore(StatusReserva.DISPONIVEL, LocalDateTime.now())
                .forEach(r -> r.setStatus(StatusReserva.EXPIRADA));
    }

    private void criarNotificacao(Usuario usuario, String nomeProduto, Reserva reserva) {
        notificacaoRepository.save(Notificacao.builder()
                .usuario(usuario)
                .titulo("Produto disponível!")
                .mensagem("O produto \"" + nomeProduto + "\" está disponível. "
                        + "Confirme sua reserva em até 3 dias.")
                .reserva(reserva)
                .build());
    }

    private Reserva buscarEntidade(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reserva não encontrada."));
    }

    private void verificarProprietario(Reserva reserva, String email, Authentication auth) {
        boolean isProprietario = reserva.getCliente().getEmail().equals(email);
        boolean isAdminOuFarmaceutico = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR")
                        || a.getAuthority().equals("ROLE_FARMACEUTICO"));
        if (!isProprietario && !isAdminOuFarmaceutico) {
            throw new AccessDeniedException("Sem permissão para acessar esta reserva.");
        }
    }
}
