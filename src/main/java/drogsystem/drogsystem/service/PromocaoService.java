package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.PromocaoCadastroDTO;
import drogsystem.drogsystem.dto.PromocaoResponseDTO;
import drogsystem.drogsystem.entity.Promocao;
import drogsystem.drogsystem.repository.PromocaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromocaoService {

    private final PromocaoRepository promocaoRepository;

    @Transactional
    public PromocaoResponseDTO cadastrar(PromocaoCadastroDTO dto) {
        if (dto.dataFim().isBefore(dto.dataInicio())) {
            throw new IllegalArgumentException("Data de fim não pode ser anterior à data de início.");
        }
        Promocao promocao = Promocao.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .tipoDesconto(dto.tipoDesconto())
                .valorDesconto(dto.valorDesconto())
                .dataInicio(dto.dataInicio())
                .dataFim(dto.dataFim())
                .build();
        return PromocaoResponseDTO.from(promocaoRepository.save(promocao));
    }

    @Transactional(readOnly = true)
    public List<PromocaoResponseDTO> listar() {
        return promocaoRepository.findAll().stream()
                .map(PromocaoResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PromocaoResponseDTO> listarVigentes() {
        return promocaoRepository.findVigentes(LocalDate.now()).stream()
                .map(PromocaoResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public PromocaoResponseDTO buscarPorId(Long id) {
        return promocaoRepository.findById(id)
                .map(PromocaoResponseDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Promoção não encontrada."));
    }

    @Transactional
    public void desativar(Long id) {
        Promocao promocao = promocaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Promoção não encontrada."));
        promocao.setAtiva(false);
        promocaoRepository.save(promocao);
    }
}
