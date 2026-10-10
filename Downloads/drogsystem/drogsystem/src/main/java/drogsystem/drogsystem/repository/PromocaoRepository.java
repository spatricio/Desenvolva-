package drogsystem.drogsystem.repository;

import drogsystem.drogsystem.entity.Promocao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface PromocaoRepository extends JpaRepository<Promocao, Long> {

    List<Promocao> findByAtivaTrue();

    @Query("SELECT p FROM Promocao p WHERE p.ativa = true AND :hoje BETWEEN p.dataInicio AND p.dataFim")
    List<Promocao> findVigentes(LocalDate hoje);
}
