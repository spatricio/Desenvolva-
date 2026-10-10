package drogsystem.drogsystem.repository;

import drogsystem.drogsystem.entity.Reserva;
import drogsystem.drogsystem.enums.StatusReserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByClienteIdOrderByDataReservaDesc(Long clienteId);

    List<Reserva> findByProdutoIdAndStatus(Long produtoId, StatusReserva status);

    List<Reserva> findByStatusAndDataExpiracaoBefore(StatusReserva status, LocalDateTime dataLimite);

    List<Reserva> findAllByOrderByDataReservaDesc();
}
