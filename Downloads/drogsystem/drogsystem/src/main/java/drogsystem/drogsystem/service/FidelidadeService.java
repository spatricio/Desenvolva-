package drogsystem.drogsystem.service;

import drogsystem.drogsystem.entity.ClientePerfil;
import drogsystem.drogsystem.enums.NivelFidelidade;
import drogsystem.drogsystem.repository.ClientePerfilRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class FidelidadeService {

    private final ClientePerfilRepository clientePerfilRepository;

    // valida e calcula o valor do desconto — 100 pontos = R$ 5,00
    @Transactional(readOnly = true)
    public BigDecimal calcularDescontoPorPontos(Long clienteId, int pontosParaResgatar) {
        ClientePerfil perfil = buscarPerfil(clienteId);
        if (pontosParaResgatar % 100 != 0) {
            throw new IllegalArgumentException("Pontos para resgatar devem ser múltiplos de 100.");
        }
        if (perfil.getPontosAcumulados() < pontosParaResgatar) {
            throw new IllegalArgumentException(
                    "Pontos insuficientes. Disponível: " + perfil.getPontosAcumulados());
        }
        return BigDecimal.valueOf((long) (pontosParaResgatar / 100) * 5);
    }

    // chamado após conclusão de venda
    @Transactional
    public void registrarCompra(Long clienteId, BigDecimal totalFinal, int pontosGerados, int pontosResgatados) {
        ClientePerfil perfil = buscarPerfil(clienteId);
        perfil.setPontosAcumulados(perfil.getPontosAcumulados() + pontosGerados - pontosResgatados);
        perfil.setTotalGasto(perfil.getTotalGasto().add(totalFinal));
        perfil.setDataUltimaCompra(LocalDateTime.now());
        perfil.setNivel(calcularNivel(perfil.getTotalGasto()));
        clientePerfilRepository.save(perfil);
    }

    // chamado ao cancelar venda — estorna os pontos
    @Transactional
    public void cancelarCompra(Long clienteId, int pontosGerados, int pontosResgatados) {
        ClientePerfil perfil = buscarPerfil(clienteId);
        int saldoAjustado = perfil.getPontosAcumulados() - pontosGerados + pontosResgatados;
        perfil.setPontosAcumulados(Math.max(0, saldoAjustado));
        clientePerfilRepository.save(perfil);
    }

    private ClientePerfil buscarPerfil(Long clienteId) {
        return clientePerfilRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Perfil de cliente não encontrado. O usuário precisa ter perfil CLIENTE."));
    }

    private NivelFidelidade calcularNivel(BigDecimal totalGasto) {
        if (totalGasto.compareTo(BigDecimal.valueOf(3000)) >= 0)
            return NivelFidelidade.PLATINA;
        if (totalGasto.compareTo(BigDecimal.valueOf(1000)) >= 0)
            return NivelFidelidade.OURO;
        if (totalGasto.compareTo(BigDecimal.valueOf(300)) >= 0)
            return NivelFidelidade.PRATA;
        return NivelFidelidade.BRONZE;
    }
}
