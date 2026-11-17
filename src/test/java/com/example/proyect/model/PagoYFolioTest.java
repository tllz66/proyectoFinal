package com.example.proyect.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagoYFolioTest {

    private final LocalDate hoy = LocalDate.now();

    @Test
    void pagoNoPuedeSerCero() {
        assertThrows(IllegalArgumentException.class, () -> new Pago(0, MedioPago.EFECTIVO, hoy));
    }

    @Test
    void pagoNegativoEsDevolucion() {
        assertTrue(new Pago(-5000, MedioPago.EFECTIVO, hoy).esDevolucion());
        assertFalse(new Pago(5000, MedioPago.TARJETA, hoy).esDevolucion());
    }

    @Test
    void pagoExigeMedioYFecha() {
        assertThrows(IllegalArgumentException.class, () -> new Pago(100, null, hoy));
        assertThrows(IllegalArgumentException.class, () -> new Pago(100, MedioPago.EFECTIVO, null));
    }

    @Test
    void folioSumaCargosDeDistintoTipoConPolimorfismo() {
        Folio folio = new Folio();
        folio.agregarCargo(new CargoAlojamiento("Estancia", hoy, 2, 400000));
        folio.agregarCargo(new CargoServicio("Desayunos", hoy, "Alimentos", 3, 20000));
        folio.agregarCargo(new CargoPenalidad("Daño", hoy, "Vidrio roto", 50000));
        assertEquals(510000, folio.calcularTotalCargos());
    }

    @Test
    void saldoEsCargosMenosPagosYLasDevolucionesLoAumentan() {
        Folio folio = new Folio();
        folio.agregarCargo(new CargoAlojamiento("Estancia", hoy, 1, 100000));
        folio.agregarPago(new Pago(100000, MedioPago.TARJETA, hoy));
        assertEquals(0, folio.calcularSaldo());
        folio.agregarPago(new Pago(-30000, MedioPago.EFECTIVO, hoy));
        assertEquals(30000, folio.calcularSaldo());
    }

    @Test
    void folioRechazaCargoOPagoNulo() {
        Folio folio = new Folio();
        assertThrows(IllegalArgumentException.class, () -> folio.agregarCargo(null));
        assertThrows(IllegalArgumentException.class, () -> folio.agregarPago(null));
    }

    @Test
    void cargoServicioExigeCantidadPositiva() {
        assertThrows(IllegalArgumentException.class,
                () -> new CargoServicio("X", hoy, "Lavandería", 0, 1000));
    }
}
