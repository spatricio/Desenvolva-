package drogsystem.drogsystem.repository;

import drogsystem.drogsystem.entity.Venda;
import drogsystem.drogsystem.enums.StatusVenda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VendaRepository extends JpaRepository<Venda, Long> {

    List<Venda> findByStatusOrderByDataVendaDesc(StatusVenda status);

    List<Venda> findByAtendenteIdOrderByDataVendaDesc(Long atendenteId);

    List<Venda> findByClienteIdOrderByDataVendaDesc(Long clienteId);

    List<Venda> findByDataVendaBetweenOrderByDataVendaDesc(LocalDateTime inicio, LocalDateTime fim);
}
