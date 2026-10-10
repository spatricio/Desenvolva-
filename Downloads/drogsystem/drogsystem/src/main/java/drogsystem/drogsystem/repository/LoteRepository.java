package drogsystem.drogsystem.repository;

import drogsystem.drogsystem.entity.Lote;
import drogsystem.drogsystem.entity.Produto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByProduto(Produto produto);

    Optional<Lote> findByProdutoAndNumeroLote(Produto produto, String numeroLote);

    List<Lote> findByDataValidadeBefore(LocalDate data);

    List<Lote> findByDataValidadeBetween(LocalDate inicio, LocalDate fim);
}
