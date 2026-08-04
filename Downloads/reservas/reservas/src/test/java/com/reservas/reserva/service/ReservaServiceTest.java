package com.reservas.reserva.service;

import com.reservas.reserva.dto.ReservaRequest;
import com.reservas.reserva.model.Reserva;
import com.reservas.reserva.repository.ReservaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock
    private ReservaRepository reservaRepository;

    @InjectMocks
    private ReservaService reservaService;

    private ReservaRequest requestValido;
    private Reserva reservaSalva;

    @BeforeEach
    void setUp() {
        requestValido = new ReservaRequest();
        requestValido.setNomeCliente("Joao Silva");
        requestValido.setEmailCliente("joao@email.com");
        requestValido.setDataCheckIn(LocalDate.now().plusDays(1));
        requestValido.setDataCheckOut(LocalDate.now().plusDays(5));
        requestValido.setQuantidadePessoas(2);
        requestValido.setObservacoes("Observacao teste");

        reservaSalva = new Reserva();
        reservaSalva.setId(1L);
        reservaSalva.setNomeCliente("Joao Silva");
        reservaSalva.setEmailCliente("joao@email.com");
        reservaSalva.setDataCheckIn(LocalDate.now().plusDays(1));
        reservaSalva.setDataCheckOut(LocalDate.now().plusDays(5));
        reservaSalva.setQuantidadePessoas(2);
    }

    @Test
    void deveCriarReservaComSucesso() {
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaSalva);

        Reserva resultado = reservaService.criar(requestValido);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNomeCliente()).isEqualTo("Joao Silva");
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void deveLancarExcecaoQuandoCheckOutAnteriorAoCheckIn() {
        requestValido.setDataCheckOut(LocalDate.now().plusDays(1));
        requestValido.setDataCheckIn(LocalDate.now().plusDays(3));

        assertThatThrownBy(() -> reservaService.criar(requestValido))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("check-out");

        verify(reservaRepository, never()).save(any());
    }

    @Test
    void deveListarTodasAsReservas() {
        when(reservaRepository.findAll()).thenReturn(List.of(reservaSalva));

        List<Reserva> resultado = reservaService.listarTodas();

        assertThat(resultado).hasSize(1);
        verify(reservaRepository, times(1)).findAll();
    }

    @Test
    void deveBuscarReservaPorId() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaSalva));

        Reserva resultado = reservaService.buscarPorId(1L);

        assertThat(resultado.getId()).isEqualTo(1L);
    }

    @Test
    void deveLancarExcecaoQuandoReservaNaoEncontrada() {
        when(reservaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservaService.buscarPorId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("nao encontrada");
    }

    @Test
    void deveAtualizarReservaComSucesso() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaSalva));
        when(reservaRepository.save(any(Reserva.class))).thenReturn(reservaSalva);

        Reserva resultado = reservaService.atualizar(1L, requestValido);

        assertThat(resultado).isNotNull();
        verify(reservaRepository, times(1)).save(any(Reserva.class));
    }

    @Test
    void deveRemoverReservaComSucesso() {
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reservaSalva));
        doNothing().when(reservaRepository).delete(reservaSalva);

        reservaService.remover(1L);

        verify(reservaRepository, times(1)).delete(reservaSalva);
    }
}
