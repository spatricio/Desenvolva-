package drogsystem.drogsystem.repository;

import drogsystem.drogsystem.entity.Produto;
import drogsystem.drogsystem.enums.CategoriaProduto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    List<Produto> findByCategoriaAndAtivoTrue(CategoriaProduto categoria);

    List<Produto> findByAtivoTrue();

    Optional<Produto> findByCodigoBarras(String codigoBarras);

    boolean existsByCodigoBarras(String codigoBarras);

    @Query("SELECT p FROM Produto p WHERE p.quantidadeEstoque < p.estoqueMinimo AND p.ativo = true")
    List<Produto> findAbaixoDoEstoqueMinimo();
}
