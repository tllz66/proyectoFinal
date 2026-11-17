package com.example.proyect.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ApartamentoTest {

    private Apartamento apto() {
        return new Apartamento("A1", "Cafetal", 1, 2, "Balcón", 90000, 120000, 160000);
    }

    @Test
    void nacePreparadoYActivo() {
        Apartamento a = apto();
        assertEquals(EstadoApartamento.PREPARADO, a.getEstado());
        assertTrue(a.isActivo());
    }

    @Test
    void tarifaDependeDeLaTemporada() {
        Apartamento a = apto();
        assertEquals(90000, a.getTarifa(Temporada.BAJA));
        assertEquals(120000, a.getTarifa(Temporada.MEDIA));
        assertEquals(160000, a.getTarifa(Temporada.ALTA));
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Apartamento("", "X", 1, 2, "", 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Apartamento("A", "X", 0, 2, "", 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new Apartamento("A", "X", 1, 2, "", 0, 1, 1));
    }

    @Test
    void cambiarEstadoYDesactivar() {
        Apartamento a = apto();
        a.cambiarEstado(EstadoApartamento.OCUPADO);
        assertEquals(EstadoApartamento.OCUPADO, a.getEstado());
        a.desactivar();
        assertFalse(a.isActivo());
    }

    @Test
    void registraNovedades() {
        Apartamento a = apto();
        a.registrarNovedad(new Novedad(java.time.LocalDateTime.now(), TipoNovedad.DANIO, "Vidrio"));
        assertEquals(1, a.obtenerNovedades().size());
    }

    @Test
    void modificarDatosValidaElNombre() {
        Apartamento a = apto();
        a.modificarDatos("Nuevo", "Jacuzzi");
        assertEquals("Nuevo", a.getNombre());
        assertEquals("Jacuzzi", a.getCaracteristicas());
        assertThrows(IllegalArgumentException.class, () -> a.modificarDatos(" ", "x"));
    }
}
