package com.reservas.reserva.service;

import com.reservas.reserva.dto.ReservaRequest;
import com.reservas.reserva.model.Reserva;
import com.reservas.reserva.repository.ReservaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReservaService {

    private final ReservaRepository reservaRepository;

    public ReservaService(ReservaRepository reservaRepository) {
        this.reservaRepository = reservaRepository;
    }

    public List<Reserva> listarTodas() {
        return reservaRepository.findAll();
    }

    public Reserva buscarPorId(Long id) {
        return reservaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reserva nao encontrada"));
    }

    public Reserva criar(ReservaRequest request) {
        validarPeriodo(request);
        Reserva reserva = new Reserva();
        preencherDados(request, reserva);
        return reservaRepository.save(reserva);
    }

    public Reserva atualizar(Long id, ReservaRequest request) {
        validarPeriodo(request);
        Reserva existente = buscarPorId(id);
        preencherDados(request, existente);
        return reservaRepository.save(existente);
    }

    public void remover(Long id) {
        Reserva existente = buscarPorId(id);
        reservaRepository.delete(existente);
    }

    private static void validarPeriodo(ReservaRequest request) {
        if (!request.getDataCheckOut().isAfter(request.getDataCheckIn())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A data de check-out deve ser posterior a data de check-in"
            );
        }
    }

    private static void preencherDados(ReservaRequest request, Reserva reserva) {
        reserva.setNomeCliente(request.getNomeCliente());
        reserva.setEmailCliente(request.getEmailCliente());
        reserva.setDataCheckIn(request.getDataCheckIn());
        reserva.setDataCheckOut(request.getDataCheckOut());
        reserva.setQuantidadePessoas(request.getQuantidadePessoas());
        reserva.setObservacoes(request.getObservacoes());
    }
}
