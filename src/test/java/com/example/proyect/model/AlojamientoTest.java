package com.example.proyect.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlojamientoTest {

    private final LocalDate hoy = LocalDate.now();
    private Alojamiento aloj;
    private Huesped ana;

    @BeforeEach
    void preparar() {
        aloj = new Alojamiento("900", "Aparta-Suites UQ", "Salento");
        ana = new Huesped("Ana", "1001", "a@m.co", "300", "ana", "ana123", LocalDate.of(1990, 1, 1));
        aloj.registrarUsuario(ana);
        aloj.registrarApartamento(new Apartamento("A1", "Cafetal", 1, 4, "", 100000, 150000, 200000));
    }

    private List<Ocupante> dosAdultos() {
        return List.of(new Ocupante("Ana", LocalDate.of(1990, 1, 1)),
                new Ocupante("Pedro", LocalDate.of(1988, 1, 1)));
    }

    private Reserva reserva(String codigo, LocalDate entrada, LocalDate salida, CanalReserva canal) {
        long valor = aloj.cotizarEstancia("A1", entrada, salida, dosAdultos());
        return new Reserva(codigo, entrada, salida, valor, ana, aloj.buscarApartamento("A1"),
                dosAdultos(), canal);
    }

    // ---------- Usuarios ----------

    @Test
    void noPermiteDocumentoNiUsuarioRepetido() {
        assertThrows(ReglaNegocioException.class, () -> aloj.registrarUsuario(
                new Huesped("Otra", "1001", "o@m.co", "1", "otra", "x", LocalDate.of(1990, 1, 1))));
        assertThrows(ReglaNegocioException.class, () -> aloj.registrarUsuario(
                new Huesped("Otra", "2002", "o@m.co", "1", "ana", "x", LocalDate.of(1990, 1, 1))));
    }

    @Test
    void iniciarSesionDevuelveElUsuarioONull() {
        assertSame(ana, aloj.iniciarSesion("ana", "ana123"));
        assertNull(aloj.iniciarSesion("ana", "mala"));
    }

    @Test
    void desvincularImpideIniciarSesion() {
        aloj.desvincularUsuario("1001");
        assertNull(aloj.iniciarSesion("ana", "ana123"));
        assertThrows(ReglaNegocioException.class, () -> aloj.desvincularUsuario("1001"));
    }

    @Test
    void editarUsuarioActualizaLosDatos() {
        aloj.editarUsuario("1001", "Ana Ruiz", "nuevo@m.co", "999");
        assertEquals("Ana Ruiz", aloj.buscarUsuario("1001").getNombre());
    }

    // ---------- Cotización y temporadas ----------

    @Test
    void cotizaNochePorNochePorPersona() {
        // 2 noches en temporada baja x 2 personas x 100000
        assertEquals(400000, aloj.cotizarEstancia("A1", hoy.plusDays(5), hoy.plusDays(7), dosAdultos()));
    }

    @Test
    void menorDeSeisNoPagaPeroOcupaCapacidad() {
        List<Ocupante> conMenor = List.of(new Ocupante("Ana", LocalDate.of(1990, 1, 1)),
                new Ocupante("Mateo", hoy.minusYears(3)));
        assertEquals(200000, aloj.cotizarEstancia("A1", hoy.plusDays(5), hoy.plusDays(7), conMenor));
    }

    @Test
    void aplicaLaTarifaDeLaTemporadaDeCadaNoche() {
        aloj.definirTemporada(new PeriodoTemporada(Temporada.MEDIA, hoy.plusDays(30), hoy.plusDays(33)));
        List<Ocupante> uno = List.of(new Ocupante("Ana", LocalDate.of(1990, 1, 1)));
        // noche del dia 29 es baja (100000) y la del dia 30 es media (150000)
        assertEquals(250000, aloj.cotizarEstancia("A1", hoy.plusDays(29), hoy.plusDays(31), uno));
        assertEquals(Temporada.BAJA, aloj.temporadaDe(hoy.plusDays(29)));
        assertEquals(Temporada.MEDIA, aloj.temporadaDe(hoy.plusDays(30)));
    }

    @Test
    void rechazaPeriodosDeTemporadaQueSeCruzan() {
        aloj.definirTemporada(new PeriodoTemporada(Temporada.MEDIA, hoy.plusDays(30), hoy.plusDays(33)));
        assertThrows(ReglaNegocioException.class, () -> aloj.definirTemporada(
                new PeriodoTemporada(Temporada.ALTA, hoy.plusDays(32), hoy.plusDays(40))));
    }

    @Test
    void rechazaEstanciaSinNoches() {
        assertThrows(ReglaNegocioException.class,
                () -> aloj.cotizarEstancia("A1", hoy.plusDays(5), hoy.plusDays(5), dosAdultos()));
    }

    // ---------- Reservas y doble venta ----------

    @Test
    void creaReservaPendienteConValorYPoliticaCongelados() {
        Reserva r = reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto());
        assertTrue(aloj.crearReserva(r));
        assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
        assertEquals(400000, r.getFolio().calcularTotalCargos());
        assertNotNull(r.getPolitica());
    }

    @Test
    void noPermiteDobleVentaSinImportarElCanal() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertThrows(ReglaNegocioException.class,
                () -> aloj.crearReserva(reserva("R2", hoy.plusDays(6), hoy.plusDays(8), new CanalPortal())));
    }

    @Test
    void laNocheDeSalidaPermiteOtraReservaQueEntraEseDia() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertTrue(aloj.crearReserva(reserva("R2", hoy.plusDays(7), hoy.plusDays(9), new CanalDirecto())));
    }

    @Test
    void rechazaSuperarLaCapacidadYFechasPasadas() {
        List<Ocupante> cinco = List.of(
                new Ocupante("A", LocalDate.of(1990, 1, 1)), new Ocupante("B", LocalDate.of(1990, 1, 1)),
                new Ocupante("C", LocalDate.of(1990, 1, 1)), new Ocupante("D", LocalDate.of(1990, 1, 1)),
                new Ocupante("E", LocalDate.of(1990, 1, 1)));
        long valor = aloj.cotizarEstancia("A1", hoy.plusDays(5), hoy.plusDays(6), cinco);
        Reserva exceso = new Reserva("R9", hoy.plusDays(5), hoy.plusDays(6), valor, ana,
                aloj.buscarApartamento("A1"), cinco, new CanalDirecto());
        assertThrows(ReglaNegocioException.class, () -> aloj.crearReserva(exceso));

        Reserva pasada = new Reserva("R8", hoy.minusDays(2), hoy.minusDays(1), 100, ana,
                aloj.buscarApartamento("A1"), dosAdultos(), new CanalDirecto());
        assertThrows(ReglaNegocioException.class, () -> aloj.crearReserva(pasada));
    }

    @Test
    void rechazaCodigoDeReservaRepetido() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertThrows(ReglaNegocioException.class,
                () -> aloj.crearReserva(reserva("R1", hoy.plusDays(20), hoy.plusDays(21), new CanalDirecto())));
    }

    // ---------- Disponibilidad y bloqueos ----------

    @Test
    void disponibilidadExcluyeReservadosBloqueadosYSinCapacidad() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertTrue(aloj.consultarDisponibles(hoy.plusDays(5), hoy.plusDays(7), 2).isEmpty());
        assertEquals(1, aloj.consultarDisponibles(hoy.plusDays(10), hoy.plusDays(12), 2).size());
        assertTrue(aloj.consultarDisponibles(hoy.plusDays(10), hoy.plusDays(12), 5).isEmpty());

        aloj.registrarBloqueo(new Bloqueo("A1", hoy.plusDays(10), hoy.plusDays(12), "Mantenimiento"));
        assertTrue(aloj.consultarDisponibles(hoy.plusDays(10), hoy.plusDays(12), 2).isEmpty());
    }

    @Test
    void noPermiteBloquearSobreUnaReservaActiva() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertThrows(ReglaNegocioException.class, () -> aloj.registrarBloqueo(
                new Bloqueo("A1", hoy.plusDays(6), hoy.plusDays(8), "Pintura")));
    }

    // ---------- Anticipo y confirmación ----------

    @Test
    void portalSeConfirmaAlCubrirElAnticipoDel30PorCiento() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalPortal()));
        aloj.registrarPago("R1", new Pago(100000, MedioPago.TRANSFERENCIA, hoy));
        assertEquals(EstadoReserva.PENDIENTE, aloj.buscarReserva("R1").getEstado());
        aloj.registrarPago("R1", new Pago(20000, MedioPago.TRANSFERENCIA, hoy)); // 120000 = 30 % de 400000
        assertEquals(EstadoReserva.CONFIRMADA, aloj.buscarReserva("R1").getEstado());
    }

    @Test
    void noConfirmaPortalSinAnticipoYElMensajeDiceCuantoFalta() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalPortal()));
        ReglaNegocioException ex = assertThrows(ReglaNegocioException.class, () -> aloj.confirmarReserva("R1"));
        assertTrue(ex.getMessage().contains("anticipo"));
    }

    @Test
    void directaSeConfirmaSinAnticipo() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertTrue(aloj.confirmarReserva("R1"));
        assertEquals(EstadoReserva.CONFIRMADA, aloj.buscarReserva("R1").getEstado());
    }

    // ---------- Canal externo ----------

    @Test
    void externaChocandoSeRechazaYQuedaRegistradaComoConflicto() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertThrows(ReglaNegocioException.class, () -> aloj.recibirReservaExterna(
                reserva("RX", hoy.plusDays(6), hoy.plusDays(8), new CanalExterno("BKG-1"))));
        assertEquals(1, aloj.getConflictos().size());
    }

    @Test
    void externaConCodigoRepetidoSeRechaza() {
        aloj.recibirReservaExterna(reserva("RX1", hoy.plusDays(5), hoy.plusDays(7), new CanalExterno("BKG-1")));
        assertThrows(ReglaNegocioException.class, () -> aloj.recibirReservaExterna(
                reserva("RX2", hoy.plusDays(20), hoy.plusDays(22), new CanalExterno("BKG-1"))));
        assertEquals(1, aloj.getConflictos().size());
    }

    // ---------- Cancelación y devolución ----------

    @Test
    void cancelarConMasDe168HorasDevuelveTodoComoPagoNegativo() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(30), hoy.plusDays(32), new CanalDirecto()));
        aloj.registrarPago("R1", new Pago(400000, MedioPago.TARJETA, hoy));
        aloj.cancelarReserva("R1");
        Reserva r = aloj.buscarReserva("R1");
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertEquals(0, r.getFolio().calcularTotalPagos()); // pago de 400000 menos devolución de 400000
    }

    @Test
    void entre48y168HorasRetieneElCincuentaPorCiento() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(30), hoy.plusDays(32), new CanalDirecto()));
        aloj.registrarPago("R1", new Pago(400000, MedioPago.TARJETA, hoy));
        LocalDateTime ahora = hoy.plusDays(30).atTime(15, 0).minusHours(100);
        assertEquals(200000, aloj.calcularDevolucion("R1", ahora));
    }

    @Test
    void conMenosDe48HorasNoHayDevolucion() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(30), hoy.plusDays(32), new CanalDirecto()));
        aloj.registrarPago("R1", new Pago(400000, MedioPago.TARJETA, hoy));
        LocalDateTime ahora = hoy.plusDays(30).atTime(15, 0).minusHours(10);
        assertEquals(0, aloj.calcularDevolucion("R1", ahora));
    }

    @Test
    void laPoliticaSeCongelaAlCrearLaReserva() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(30), hoy.plusDays(32), new CanalDirecto()));
        aloj.registrarPago("R1", new Pago(400000, MedioPago.TARJETA, hoy));
        aloj.configurarPolitica(new PoliticaEscalonada(24, 12, 1.0)); // nueva política más dura
        LocalDateTime ahora = hoy.plusDays(30).atTime(15, 0).minusHours(100);
        assertEquals(200000, aloj.calcularDevolucion("R1", ahora)); // sigue aplicando la anterior
    }

    @Test
    void cancelaLasPendientesConMasDe24HorasSinConfirmar() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(30), hoy.plusDays(32), new CanalPortal()));
        assertEquals(0, aloj.cancelarPendientesVencidas(LocalDateTime.now().plusHours(23)));
        assertEquals(1, aloj.cancelarPendientesVencidas(LocalDateTime.now().plusHours(25)));
        assertEquals(EstadoReserva.CANCELADA, aloj.buscarReserva("R1").getEstado());
    }

    @Test
    void noSeCancelaUnaReservaFinalizada() {
        aloj.crearReserva(reserva("R1", hoy, hoy.plusDays(2), new CanalDirecto()));
        aloj.confirmarReserva("R1");
        aloj.hacerCheckIn("R1");
        aloj.hacerCheckOut("R1");
        assertThrows(ReglaNegocioException.class, () -> aloj.cancelarReserva("R1"));
    }

    // ---------- Check-in, check-out y no-show ----------

    @Test
    void checkInOcupaElApartamentoYCheckOutLoDejaPendienteDePreparacion() {
        aloj.crearReserva(reserva("R1", hoy, hoy.plusDays(2), new CanalDirecto()));
        aloj.confirmarReserva("R1");
        aloj.hacerCheckIn("R1");
        assertEquals(EstadoApartamento.OCUPADO, aloj.buscarApartamento("A1").getEstado());
        aloj.hacerCheckOut("R1");
        assertEquals(EstadoApartamento.PENDIENTE_PREPARACION, aloj.buscarApartamento("A1").getEstado());
        assertEquals(EstadoReserva.FINALIZADA, aloj.buscarReserva("R1").getEstado());
    }

    @Test
    void checkInExigeReservaConfirmadaYFechaDeEntrada() {
        aloj.crearReserva(reserva("R1", hoy, hoy.plusDays(2), new CanalDirecto()));
        assertThrows(ReglaNegocioException.class, () -> aloj.hacerCheckIn("R1")); // sigue Pendiente
        aloj.crearReserva(reserva("R2", hoy.plusDays(10), hoy.plusDays(12), new CanalDirecto()));
        aloj.confirmarReserva("R2");
        assertThrows(ReglaNegocioException.class, () -> aloj.hacerCheckIn("R2")); // aún no llega
    }

    @Test
    void noShowSoloDesdeLas10PMDelDiaDeEntrada() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(10), hoy.plusDays(12), new CanalDirecto()));
        aloj.confirmarReserva("R1");
        assertThrows(ReglaNegocioException.class, () -> aloj.declararNoShow("R1"));
    }

    // ---------- Apartamentos ----------

    @Test
    void fueraDeServicioExigeMotivo() {
        assertThrows(ReglaNegocioException.class,
                () -> aloj.cambiarEstadoApartamento("A1", EstadoApartamento.FUERA_DE_SERVICIO, " "));
        aloj.cambiarEstadoApartamento("A1", EstadoApartamento.FUERA_DE_SERVICIO, "Plomería");
        assertEquals("Plomería", aloj.buscarApartamento("A1").getMotivoFueraServicio());
    }

    @Test
    void noEliminaUnApartamentoConReservaActivaPeroSiUnoLibre() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertThrows(ReglaNegocioException.class, () -> aloj.eliminarApartamento("A1"));
        aloj.registrarApartamento(new Apartamento("A2", "Cocora", 1, 2, "", 1, 1, 1));
        assertTrue(aloj.eliminarApartamento("A2"));
        assertFalse(aloj.buscarApartamento("A2").isActivo());
    }

    @Test
    void modificarApartamentoCambiaNombreYTarifas() {
        aloj.modificarApartamento("A1", "Nuevo", "Jacuzzi", 110000, 160000, 210000);
        assertEquals("Nuevo", aloj.buscarApartamento("A1").getNombre());
        assertEquals(160000, aloj.buscarApartamento("A1").getTarifa(Temporada.MEDIA));
        assertThrows(ReglaNegocioException.class,
                () -> aloj.modificarApartamento("A1", "", "x", 1, 1, 1));
    }

    // ---------- Reportes ----------

    @Test
    void ingresosDelPeriodoSumanPagosMenosDevoluciones() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(30), hoy.plusDays(32), new CanalDirecto()));
        aloj.registrarPago("R1", new Pago(400000, MedioPago.TARJETA, hoy));
        assertEquals(400000, aloj.calcularIngresosPorPeriodo(hoy, hoy));
        aloj.cancelarReserva("R1"); // devolución total
        assertEquals(0, aloj.calcularIngresosPorPeriodo(hoy, hoy));
    }

    @Test
    void saldosPendientesExcluyenCanceladas() {
        aloj.crearReserva(reserva("R1", hoy.plusDays(5), hoy.plusDays(7), new CanalDirecto()));
        assertEquals(1, aloj.reporteSaldosPendientes().size());
        aloj.cancelarReserva("R1");
        assertTrue(aloj.reporteSaldosPendientes().isEmpty());
    }

    @Test
    void reportePorCanalIncluyeLosTresCanales() {
        assertEquals(3, aloj.reporteReservasPorCanal().size());
    }
}
