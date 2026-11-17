package com.example.proyect.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservaTest {

    private final LocalDate entrada = LocalDate.now().plusDays(10);
    private Huesped titular;
    private Apartamento apto;

    @BeforeEach
    void preparar() {
        titular = new Huesped("Ana", "1", "a@m.co", "300", "ana", "x1", LocalDate.of(1990, 1, 1));
        apto = new Apartamento("A1", "Cafetal", 1, 4, "", 100000, 150000, 200000);
    }

    private Reserva reserva(List<Ocupante> ocupantes) {
        return new Reserva("R1", entrada, entrada.plusDays(2), 400000, titular, apto, ocupantes, new CanalDirecto());
    }

    private List<Ocupante> adultos() {
        return List.of(new Ocupante("Ana", LocalDate.of(1990, 1, 1)),
                new Ocupante("Pedro", LocalDate.of(1988, 1, 1)));
    }

    @Test
    void nacePendienteYCuentaLasNoches() {
        Reserva r = reserva(adultos());
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(2, r.calcularNoches());
    }

    @Test
    void losMenoresDeSeisAniosNoPagan() {
        Reserva r = reserva(List.of(new Ocupante("Ana", LocalDate.of(1990, 1, 1)),
                new Ocupante("Mateo", entrada.minusYears(3)),
                new Ocupante("Sofi", entrada.minusYears(6))));
        assertEquals(2, r.contarOcupantesQuePagan());
    }

    @Test
    void cicloDeVidaCompleto() {
        Reserva r = reserva(adultos());
        assertTrue(r.cambiarEstado(EstadoReserva.CONFIRMADA));
        assertTrue(r.cambiarEstado(EstadoReserva.EN_CURSO));
        assertTrue(r.cambiarEstado(EstadoReserva.FINALIZADA));
        assertFalse(r.estaActiva());
    }

    @Test
    void rechazaTransicionesInvalidas() {
        Reserva r = reserva(adultos());
        assertFalse(r.cambiarEstado(EstadoReserva.EN_CURSO));   // Pendiente no salta a En curso
        assertFalse(r.cambiarEstado(EstadoReserva.FINALIZADA));
        r.cambiarEstado(EstadoReserva.CANCELADA);
        assertFalse(r.cambiarEstado(EstadoReserva.CONFIRMADA)); // Cancelada es final
    }

    @Test
    void laNocheDeSalidaNoSeOcupa() {
        Reserva r = reserva(adultos());
        assertFalse(r.seCruzaCon(entrada.plusDays(2), entrada.plusDays(4)));
        assertTrue(r.seCruzaCon(entrada.plusDays(1), entrada.plusDays(3)));
    }

    @Test
    void validaLosDatosObligatorios() {
        assertThrows(IllegalArgumentException.class, () -> new Reserva("", entrada, entrada.plusDays(1),
                1, titular, apto, adultos(), new CanalDirecto()));
        assertThrows(IllegalArgumentException.class, () -> new Reserva("R", entrada, entrada,
                1, titular, apto, adultos(), new CanalDirecto()));
    }
}
