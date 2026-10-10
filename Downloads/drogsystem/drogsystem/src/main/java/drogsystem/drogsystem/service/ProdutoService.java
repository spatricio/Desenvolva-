package drogsystem.drogsystem.service;

import drogsystem.drogsystem.dto.ProdutoCadastroDTO;
import drogsystem.drogsystem.dto.ProdutoResponseDTO;
import drogsystem.drogsystem.entity.Produto;
import drogsystem.drogsystem.enums.CategoriaProduto;
import drogsystem.drogsystem.repository.ProdutoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    @Transactional
    public ProdutoResponseDTO cadastrar(ProdutoCadastroDTO dto) {
        if (dto.codigoBarras() != null && produtoRepository.existsByCodigoBarras(dto.codigoBarras())) {
            throw new IllegalArgumentException("Código de barras já cadastrado.");
        }
        Produto produto = Produto.builder()
                .nome(dto.nome())
                .descricao(dto.descricao())
                .codigoBarras(dto.codigoBarras())
                .categoria(dto.categoria())
                .unidadeMedida(dto.unidadeMedida())
                .estoqueMinimo(dto.estoqueMinimo() != null ? dto.estoqueMinimo() : 0)
                .preco(dto.preco())
                .build();
        return ProdutoResponseDTO.from(produtoRepository.save(produto));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listar() {
        return produtoRepository.findByAtivoTrue().stream()
                .map(ProdutoResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProdutoResponseDTO buscarPorId(Long id) {
        return produtoRepository.findById(id)
                .map(ProdutoResponseDTO::from)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> buscarPorCategoria(CategoriaProduto categoria) {
        return produtoRepository.findByCategoriaAndAtivoTrue(categoria).stream()
                .map(ProdutoResponseDTO::from)
                .toList();
    }

    @Transactional
    public ProdutoResponseDTO atualizar(Long id, ProdutoCadastroDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        if (dto.codigoBarras() != null
                && !dto.codigoBarras().equals(produto.getCodigoBarras())
                && produtoRepository.existsByCodigoBarras(dto.codigoBarras())) {
            throw new IllegalArgumentException("Código de barras já cadastrado.");
        }
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setCodigoBarras(dto.codigoBarras());
        produto.setCategoria(dto.categoria());
        produto.setUnidadeMedida(dto.unidadeMedida());
        produto.setEstoqueMinimo(dto.estoqueMinimo() != null ? dto.estoqueMinimo() : 0);
        produto.setPreco(dto.preco());
        return ProdutoResponseDTO.from(produtoRepository.save(produto));
    }

    @Transactional
    public void desativar(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado."));
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public List<ProdutoResponseDTO> listarAbaixoDoEstoqueMinimo() {
        return produtoRepository.findAbaixoDoEstoqueMinimo().stream()
                .map(ProdutoResponseDTO::from)
                .toList();
    }
}
