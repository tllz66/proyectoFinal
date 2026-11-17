package com.example.proyect.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CanalesYPoliticaTest {

    @Test
    void portalExigeAnticipoDel30PorCiento() {
        CanalReserva canal = new CanalPortal();
        assertTrue(canal.exigeAnticipo());
        assertEquals(60000, canal.calcularAnticipo(200000));
        assertEquals(0, canal.calcularComision(200000));
        assertEquals(TipoCanal.PORTAL, canal.getTipo());
    }

    @Test
    void externoCobraComisionDel15PorCientoYNoPideAnticipo() {
        CanalReserva canal = new CanalExterno("BKG-1");
        assertFalse(canal.exigeAnticipo());
        assertEquals(0, canal.calcularAnticipo(200000));
        assertEquals(30000, canal.calcularComision(200000));
        assertEquals(TipoCanal.EXTERNA, canal.getTipo());
    }

    @Test
    void directoNoTieneAnticipoNiComision() {
        CanalReserva canal = new CanalDirecto();
        assertFalse(canal.exigeAnticipo());
        assertEquals(0, canal.calcularAnticipo(200000));
        assertEquals(0, canal.calcularComision(200000));
        assertEquals(TipoCanal.DIRECTA, canal.getTipo());
    }

    @Test
    void canalesRechazanParametrosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new CanalExterno(" "));
        assertThrows(IllegalArgumentException.class, () -> new CanalPortal(0));
        assertThrows(IllegalArgumentException.class, () -> new CanalExterno("X", 1.5));
    }

    @Test
    void politicaEscalonadaDevuelveSegunLaAntelacion() {
        PoliticaCancelacion p = new PoliticaEscalonada(168, 48, 0.50);
        assertEquals(400000, p.calcularReembolso(400000, 400000, 200)); // reembolso total
        assertEquals(200000, p.calcularReembolso(400000, 400000, 100)); // retiene el 50 %
        assertEquals(0, p.calcularReembolso(400000, 400000, 10));       // sin reembolso
    }

    @Test
    void politicaNuncaDevuelveMasDeLoPagado() {
        PoliticaCancelacion p = new PoliticaEscalonada(168, 48, 0.50);
        assertEquals(0, p.calcularReembolso(400000, 100000, 100));
    }

    @Test
    void politicaRechazaPlazosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new PoliticaEscalonada(24, 48, 0.5));
        assertThrows(IllegalArgumentException.class, () -> new PoliticaEscalonada(168, 48, 1.5));
    }
}
