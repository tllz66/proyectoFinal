package com.example.proyect.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OcupanteTest {

    @Test
    void calculaLaEdadALaFechaDeEntrada() {
        Ocupante o = new Ocupante("Mateo", LocalDate.of(2020, 6, 15));
        assertEquals(5, o.calcularEdad(LocalDate.of(2026, 6, 14)));
        assertEquals(6, o.calcularEdad(LocalDate.of(2026, 6, 15)));
    }

    @Test
    void rechazaNombreVacio() {
        assertThrows(IllegalArgumentException.class, () -> new Ocupante(" ", LocalDate.of(2000, 1, 1)));
    }

    @Test
    void rechazaFechaNula() {
        assertThrows(IllegalArgumentException.class, () -> new Ocupante("Ana", null));
    }
}
