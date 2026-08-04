package com.reservas.reserva.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reservas.reserva.dto.ReservaRequest;
import com.reservas.reserva.model.Reserva;
import com.reservas.reserva.service.ReservaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservaController.class)
class ReservaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservaService reservaService;

    private Reserva reserva;
    private ReservaRequest request;

    @BeforeEach
    void setUp() {
        reserva = new Reserva();
        reserva.setId(1L);
        reserva.setNomeCliente("Joao Silva");
        reserva.setEmailCliente("joao@email.com");
        reserva.setDataCheckIn(LocalDate.now().plusDays(1));
        reserva.setDataCheckOut(LocalDate.now().plusDays(5));
        reserva.setQuantidadePessoas(2);

        request = new ReservaRequest();
        request.setNomeCliente("Joao Silva");
        request.setEmailCliente("joao@email.com");
        request.setDataCheckIn(LocalDate.now().plusDays(1));
        request.setDataCheckOut(LocalDate.now().plusDays(5));
        request.setQuantidadePessoas(2);
    }

    @Test
    void deveListarTodasAsReservas() throws Exception {
        when(reservaService.listarTodas()).thenReturn(List.of(reserva));

        mockMvc.perform(get("/api/reservas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeCliente").value("Joao Silva"));
    }

    @Test
    void deveBuscarReservaPorId() throws Exception {
        when(reservaService.buscarPorId(1L)).thenReturn(reserva);

        mockMvc.perform(get("/api/reservas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.emailCliente").value("joao@email.com"));
    }

    @Test
    void deveRetornar404QuandoReservaNaoEncontrada() throws Exception {
        when(reservaService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva nao encontrada"));

        mockMvc.perform(get("/api/reservas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Reserva nao encontrada"));
    }

    @Test
    void deveCriarReservaComSucesso() throws Exception {
        when(reservaService.criar(any(ReservaRequest.class))).thenReturn(reserva);

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nomeCliente").value("Joao Silva"));
    }

    @Test
    void deveRetornar400QuandoDadosInvalidos() throws Exception {
        request.setNomeCliente("");
        request.setEmailCliente("email-invalido");

        mockMvc.perform(post("/api/reservas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void deveAtualizarReservaComSucesso() throws Exception {
        when(reservaService.atualizar(eq(1L), any(ReservaRequest.class))).thenReturn(reserva);

        mockMvc.perform(put("/api/reservas/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void deveRemoverReservaComSucesso() throws Exception {
        doNothing().when(reservaService).remover(1L);

        mockMvc.perform(delete("/api/reservas/1"))
                .andExpect(status().isNoContent());
    }
}
