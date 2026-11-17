package com.example.proyect.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {

    @Test
    void cadaTipoDeUsuarioTieneSuRolPolimorfico() {
        Usuario h = new Huesped("Ana", "1", "a@m.co", "300", "ana", "x1", LocalDate.of(1990, 1, 1));
        Usuario r = new Recepcionista("Jhan", "2", "j@m.co", "301", "jhan", "x2", LocalDate.of(2024, 1, 1));
        Usuario a = new Administrador("Santiago", "3", "s@m.co", "302", "san", "x3");
        assertEquals(Rol.HUESPED, h.getRol());
        assertEquals(Rol.RECEPCIONISTA, r.getRol());
        assertEquals(Rol.ADMINISTRADOR, a.getRol());
    }

    @Test
    void autenticaSoloConCredencialesCorrectas() {
        Usuario a = new Administrador("Santiago", "3", "s@m.co", "302", "san", "clave");
        assertTrue(a.autenticar("san", "clave"));
        assertFalse(a.autenticar("san", "otra"));
        assertFalse(a.autenticar("otro", "clave"));
    }

    @Test
    void usuarioDesactivadoNoPuedeIniciarSesion() {
        Usuario a = new Administrador("Santiago", "3", "s@m.co", "302", "san", "clave");
        a.desactivar();
        assertFalse(a.isActivo());
        assertFalse(a.autenticar("san", "clave"));
    }

    @Test
    void camposObligatorios() {
        assertThrows(IllegalArgumentException.class,
                () -> new Administrador(" ", "3", "s@m.co", "302", "san", "clave"));
        assertThrows(IllegalArgumentException.class,
                () -> new Administrador("S", "3", "s@m.co", "302", "san", ""));
    }

    @Test
    void huespedRechazaFechaDeNacimientoFutura() {
        assertThrows(IllegalArgumentException.class, () -> new Huesped("Ana", "1", "a@m.co", "300",
                "ana", "x1", LocalDate.now().plusDays(1)));
    }

    @Test
    void actualizarDatosCambiaNombreCorreoYTelefonoPeroNoElDocumento() {
        Usuario h = new Huesped("Ana", "1", "a@m.co", "300", "ana", "x1", LocalDate.of(1990, 1, 1));
        h.actualizarDatos("Ana Ruiz", "nuevo@m.co", "999");
        assertEquals("Ana Ruiz", h.getNombre());
        assertEquals("nuevo@m.co", h.getCorreo());
        assertEquals("999", h.getTelefono());
        assertEquals("1", h.getDocumento());
    }
}
