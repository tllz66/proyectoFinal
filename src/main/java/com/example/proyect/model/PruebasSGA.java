package com.example.proyect.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class PruebasSGA {

    private static int total = 0;
    private static int correctas = 0;

    public static void main(String[] args) {
        Alojamiento aloj = new Alojamiento("900123", "Aparta-Suites UQ", "Salento");
        LocalDate hoy = LocalDate.now();

        Huesped ana = new Huesped("Ana Ruiz", "1001", "ana@mail.com", "300", "ana", "123", LocalDate.of(1990, 5, 1));
        aloj.registrarUsuario(ana);
        Apartamento a1 = new Apartamento("A1", "Vista", 2, 4, "Balcón", 100000, 150000, 200000);
        aloj.registrarApartamento(a1);

        LocalDate e = hoy.plusDays(30), s = e.plusDays(2);
        List<Ocupante> ocupantes = List.of(
                new Ocupante("Ana", LocalDate.of(1990, 5, 1)),
                new Ocupante("Bebe", e.minusYears(3)),          // menor de 6: no paga
                new Ocupante("Niño", e.minusYears(10)));

        System.out.println("=== Cotización ===");
        long valor = aloj.cotizarEstancia("A1", e, s, ocupantes);
        verificar("2 noches x 2 pagan x 100.000", 400000L, valor);
        acepta(() -> aloj.definirTemporada(new PeriodoTemporada(Temporada.ALTA, e.plusDays(1), e.plusDays(5))));
        verificar("Cruza Baja y Alta: 2x100.000 + 2x200.000", 600000L,
                aloj.cotizarEstancia("A1", e, s, ocupantes));
        verificar("Periodo que se cruza se rechaza", false,
                acepta(() -> aloj.definirTemporada(new PeriodoTemporada(Temporada.MEDIA, e, e.plusDays(2)))));
        verificar("Desglose tiene 2 noches", 2, aloj.desglosarEstancia("A1", e, s, ocupantes).size());
        long v = aloj.cotizarEstancia("A1", e, s, ocupantes);

        System.out.println("=== Doble venta y capacidad ===");
        Reserva r1 = new Reserva("R1", e, s, v, ana, a1, ocupantes, new CanalPortal());
        verificar("Crear R1 (Portal)", true, acepta(() -> aloj.crearReserva(r1)));
        verificar("R1 nace Pendiente", EstadoReserva.PENDIENTE, r1.getEstado());
        verificar("Folio con cargo de alojamiento", v, r1.getFolio().calcularTotalCargos());
        Reserva r2 = new Reserva("R2", e.plusDays(1), s.plusDays(1),
                aloj.cotizarEstancia("A1", e.plusDays(1), s.plusDays(1), ocupantes),
                ana, a1, ocupantes, new CanalDirecto());
        verificar("Doble venta (comparte una noche) se rechaza", false, acepta(() -> aloj.crearReserva(r2)));
        Reserva r3 = new Reserva("R3", s, s.plusDays(1),
                aloj.cotizarEstancia("A1", s, s.plusDays(1), ocupantes),
                ana, a1, ocupantes, new CanalDirecto());
        verificar("Entrada = salida de otra reserva sí se permite", true, acepta(() -> aloj.crearReserva(r3)));
        verificar("Valor manipulado se rechaza", false, acepta(() -> aloj.crearReserva(new Reserva("R4",
                s.plusDays(5), s.plusDays(6), 1, ana, a1, ocupantes, new CanalDirecto()))));
        List<Ocupante> cinco = List.of(ocupantes.get(0), ocupantes.get(0), ocupantes.get(0),
                ocupantes.get(0), ocupantes.get(0));
        verificar("Más ocupantes que la capacidad se rechaza", false, acepta(() -> aloj.crearReserva(new Reserva("R5",
                s.plusDays(8), s.plusDays(9), 1, ana, a1, cinco, new CanalDirecto()))));
        verificar("Entrada anterior a hoy se rechaza", false, acepta(() -> aloj.crearReserva(new Reserva("R6",
                hoy.minusDays(2), hoy, 1, ana, a1, ocupantes, new CanalDirecto()))));

        System.out.println("=== Anticipo y ciclo de vida ===");
        verificar("Confirmar sin anticipo se rechaza", false, acepta(() -> aloj.confirmarReserva("R1")));
        aloj.registrarPago("R1", new Pago(new CanalPortal().calcularAnticipo(v), MedioPago.TRANSFERENCIA, hoy));
        verificar("Con el anticipo (30 %) queda Confirmada", EstadoReserva.CONFIRMADA, r1.getEstado());
        verificar("Anticipo = 180.000", 180000L, r1.getFolio().calcularTotalPagos());
        verificar("Saldo = 420.000", 420000L, r1.getFolio().calcularSaldo());
        verificar("Confirmada no pasa a Finalizada", false, r1.cambiarEstado(EstadoReserva.FINALIZADA));
        verificar("Check-in antes de la fecha de entrada se rechaza", false, acepta(() -> aloj.hacerCheckIn("R1")));

        System.out.println("=== Cancelación ===");
        long conMasDe7Dias = aloj.calcularDevolucion("R1", LocalDateTime.now());
        verificar("Más de 7 días: devuelve todo lo pagado", 180000L, conMasDe7Dias);
        verificar("Entre 2 y 7 días: retiene 50 % de 600.000 (devuelve 0)", 0L,
                aloj.calcularDevolucion("R1", e.minusDays(5).atTime(10, 0)));
        verificar("Menos de 48 h: devuelve 0", 0L, aloj.calcularDevolucion("R1", e.minusDays(1).atTime(10, 0)));
        verificar("Cancelar R1", true, acepta(() -> aloj.cancelarReserva("R1")));
        verificar("R1 Cancelada", EstadoReserva.CANCELADA, r1.getEstado());
        verificar("Devolución registrada como pago negativo", 0L, r1.getFolio().calcularTotalPagos());
        verificar("Cancelada no vuelve a cancelarse", false, acepta(() -> aloj.cancelarReserva("R1")));
        verificar("Cancelada se excluye de saldos pendientes", false,
                aloj.reporteSaldosPendientes().contains(r1));
        verificar("Al cancelar se liberan las noches (reservar de nuevo las de R1)", true,
                acepta(() -> aloj.crearReserva(new Reserva("R7", e, s, v, ana, a1, ocupantes, new CanalDirecto()))));

        System.out.println("=== Canal externo ===");
        LocalDate e2 = e.plusDays(20);
        long v2 = aloj.cotizarEstancia("A1", e2, e2.plusDays(1), ocupantes);
        Reserva x1 = new Reserva("X1", e2, e2.plusDays(1), v2, ana, a1, ocupantes, new CanalExterno("EXT-1"));
        verificar("Externa válida", true, acepta(() -> aloj.recibirReservaExterna(x1)));
        verificar("Comisión 15 %", Math.round(v2 * 0.15), x1.getCanal().calcularComision(v2));
        Reserva x2 = new Reserva("X2", e2, e2.plusDays(1), v2, ana, a1, ocupantes, new CanalExterno("EXT-2"));
        verificar("Externa que choca se rechaza", false, acepta(() -> aloj.recibirReservaExterna(x2)));
        verificar("Queda un conflicto de canal", 1, aloj.getConflictos().size());
        verificar("La reserva existente se conserva", EstadoReserva.PENDIENTE, x1.getEstado());
        Reserva x3 = new Reserva("X3", e2.plusDays(10), e2.plusDays(11),
                aloj.cotizarEstancia("A1", e2.plusDays(10), e2.plusDays(11), ocupantes),
                ana, a1, ocupantes, new CanalExterno("EXT-1"));
        verificar("Código externo repetido se rechaza", false, acepta(() -> aloj.recibirReservaExterna(x3)));
        verificar("Conflictos = 2", 2, aloj.getConflictos().size());

        System.out.println("=== Bloqueos, estados y pendientes vencidas ===");
        verificar("Bloqueo que choca con reserva activa se rechaza", false,
                acepta(() -> aloj.registrarBloqueo(new Bloqueo("A1", e2, e2.plusDays(3), "Obra"))));
        LocalDate libre = e.plusDays(60);
        verificar("Bloqueo sobre fechas libres", true,
                acepta(() -> aloj.registrarBloqueo(new Bloqueo("A1", libre, libre.plusDays(2), "Obra"))));
        verificar("Disponibilidad excluye noches bloqueadas", 0,
                aloj.consultarDisponibles(libre, libre.plusDays(1), 2).size());
        verificar("Fuera de servicio sin motivo se rechaza", false,
                acepta(() -> aloj.cambiarEstadoApartamento("A1", EstadoApartamento.FUERA_DE_SERVICIO, " ")));
        verificar("Fuera de servicio con motivo", true,
                acepta(() -> aloj.cambiarEstadoApartamento("A1", EstadoApartamento.FUERA_DE_SERVICIO, "Tubería")));
        acepta(() -> aloj.cambiarEstadoApartamento("A1", EstadoApartamento.PREPARADO, null));
        verificar("Pendientes con más de 24 h se cancelan (R3, R7 y X1)", 3,
                aloj.cancelarPendientesVencidas(LocalDateTime.now().plusHours(25)));

        System.out.println("=== Pagos inalterables y reportes ===");
        verificar("Estado de X1 cancelado", EstadoReserva.CANCELADA, x1.getEstado());
        verificar("Lista de pagos no modificable", true, esNoModificable(r1));
        verificar("Reporte por canal tiene 3 filas", 3, aloj.reporteReservasPorCanal().size());
        verificar("Política congelada no se reasigna", true, falla(() -> r1.asignarPolitica(aloj.getPoliticaVigente())));


        System.out.println("=== Mensajes de rechazo (para la interfaz) ===");
        LocalDate f = e.plusDays(90);
        long vf = aloj.cotizarEstancia("A1", f, f.plusDays(2), ocupantes);
        aloj.crearReserva(new Reserva("RA", f, f.plusDays(2), vf, ana, a1, ocupantes, new CanalDirecto()));
        verificar("Doble venta explica el motivo", true,
                motivo(() -> aloj.crearReserva(new Reserva("RB", f.plusDays(1), f.plusDays(3),
                        aloj.cotizarEstancia("A1", f.plusDays(1), f.plusDays(3), ocupantes),
                        ana, a1, ocupantes, new CanalDirecto()))).contains("Doble venta"));
        verificar("Fuera de servicio explica el motivo", true,
                motivo(() -> aloj.cambiarEstadoApartamento("A1", EstadoApartamento.FUERA_DE_SERVICIO, null))
                        .contains("motivo"));
        verificar("Reserva inexistente explica el motivo", true,
                motivo(() -> aloj.confirmarReserva("NO-EXISTE")).contains("No existe la reserva"));
        verificar("Cancelar una Cancelada explica el motivo", true,
                motivo(() -> aloj.cancelarReserva("R1")).contains("no se puede cancelar"));

        System.out.println("=== Datos de prueba ===");
        Alojamiento demo = DatosPrueba.crearAlojamientoConDatos();
        verificar("Cuatro apartamentos", 4, demo.getApartamentos().size());
        verificar("Cinco reservas", 5, demo.getReservas().size());
        verificar("Login del administrador", Rol.ADMINISTRADOR, demo.iniciarSesion("admin", "admin123").getRol());
        verificar("Login con clave incorrecta", null, demo.iniciarSesion("admin", "xxx"));
        verificar("RES-002 (Portal) quedó Confirmada por el anticipo", EstadoReserva.CONFIRMADA,
                demo.buscarReserva("RES-002").getEstado());
        verificar("RES-003 (Portal sin pagar) quedó Pendiente", EstadoReserva.PENDIENTE,
                demo.buscarReserva("RES-003").getEstado());
        verificar("RES-002: el menor de 3 años no paga (2 pagan)", 2,
                demo.buscarReserva("RES-002").contarOcupantesQuePagan());
        verificar("Check-in de RES-HOY", true, acepta(() -> demo.hacerCheckIn("RES-HOY")));
        verificar("Apartamento A103 queda Ocupado", EstadoApartamento.OCUPADO,
                demo.buscarApartamento("A103").getEstado());
        verificar("Check-out de RES-HOY", true, acepta(() -> demo.hacerCheckOut("RES-HOY")));
        verificar("A103 queda Pendiente de preparación", EstadoApartamento.PENDIENTE_PREPARACION,
                demo.buscarApartamento("A103").getEstado());
        System.out.println("=== Administración (modificar, editar, política, eliminar) ===");
        long valorAntes = demo.buscarReserva("RES-001").getValorEstancia();
        verificar("Modificar apartamento A101", true, acepta(() ->
                demo.modificarApartamento("A101", "Cafetal Plus", "Balcón", 100000, 130000, 170000)));
        verificar("El apartamento quedó con el nuevo nombre", "Cafetal Plus", demo.buscarApartamento("A101").getNombre());
        verificar("La reserva existente conserva su valor congelado", valorAntes,
                demo.buscarReserva("RES-001").getValorEstancia());
        verificar("Modificar con tarifa 0 se rechaza", false, acepta(() ->
                demo.modificarApartamento("A101", "X", "", 0, 1, 1)));
        verificar("Editar datos de un usuario", true, acepta(() ->
                demo.editarUsuario("1001", "Ana R. Ruiz", "ana.ruiz@mail.com", "3109999999")));
        verificar("Usuario editado", "Ana R. Ruiz", demo.buscarUsuario("1001").getNombre());
        verificar("Editar con nombre vacío se rechaza", false, acepta(() ->
                demo.editarUsuario("1001", " ", "a@a.com", "1")));
        PoliticaEscalonada nuevaPolitica = new PoliticaEscalonada(240, 72, 0.40);
        demo.configurarPolitica(nuevaPolitica);
        verificar("La reserva existente conserva su política", false,
                demo.buscarReserva("RES-001").getPolitica() == nuevaPolitica);
        verificar("Eliminar apartamento con reserva activa se rechaza", false, acepta(() ->
                demo.eliminarApartamento("A101")));
        demo.registrarApartamento(new Apartamento("A105", "Nuevo", 1, 2, "", 50000, 60000, 70000));
        verificar("Eliminar (lógico) un apartamento sin reservas", true, acepta(() -> demo.eliminarApartamento("A105")));
        verificar("Un apartamento eliminado no aparece disponible", false,
                demo.consultarDisponibles(LocalDate.now().plusDays(200), LocalDate.now().plusDays(201), 1)
                        .contains(demo.buscarApartamento("A105")));
        Huesped nuevoHuesped = new Huesped("Pedro", "2001", "p@mail.com", "300", "pedro", "p123", LocalDate.of(2000, 1, 1));
        verificar("Registrar huésped nuevo", true, acepta(() -> demo.registrarUsuario(nuevoHuesped)));
        verificar("Documento repetido se rechaza", false, acepta(() -> demo.registrarUsuario(
                new Huesped("Otro", "2001", "o@mail.com", "300", "otro", "o123", LocalDate.of(2000, 1, 1)))));
        verificar("Usuario repetido se rechaza", false, acepta(() -> demo.registrarUsuario(
                new Huesped("Otro", "2002", "o@mail.com", "300", "pedro", "o123", LocalDate.of(2000, 1, 1)))));
        Recepcionista contratada = new Recepcionista("Rosa", "8002", "r@mail.com", "300", "rosa", "r123", LocalDate.now());
        verificar("Contratar recepcionista", true, acepta(() -> demo.registrarUsuario(contratada)));
        verificar("Desvincular recepcionista", true, acepta(() -> demo.desvincularUsuario("8002")));
        verificar("Un desvinculado no puede iniciar sesión", null, demo.iniciarSesion("rosa", "r123"));
        verificar("Reserva Pendiente sin anticipo: el mensaje dice cuánto falta", true,
                motivo(() -> demo.confirmarReserva("RES-003")).contains("faltan"));

        System.out.println();
        System.out.println("RESULTADO FINAL: " + correctas + " de " + total + " verificaciones correctas");
    }

    /** true si la operación se realizó; false si se rechazó con ReglaNegocioException. */
    private static boolean acepta(BooleanSupplier op) {
        try {
            return op.getAsBoolean();
        } catch (ReglaNegocioException ex) {
            return false;
        }
    }

    /** Mensaje con el que se rechazó la operación ("" si no se rechazó). */
    private static String motivo(BooleanSupplier op) {
        try {
            op.getAsBoolean();
            return "";
        } catch (ReglaNegocioException ex) {
            return ex.getMessage();
        }
    }

    private static boolean esNoModificable(Reserva r) {
        return falla(() -> r.getFolio().getPagos().add(new Pago(1, MedioPago.EFECTIVO, LocalDate.now())));
    }

    private static boolean falla(Runnable accion) {
        try { accion.run(); return false; } catch (RuntimeException ex) { return true; }
    }

    private static void verificar(String que, Object esperado, Object obtenido) {
        total++;
        boolean ok = Objects.equals(esperado, obtenido);
        if (ok) correctas++;
        System.out.println("   " + (ok ? "[OK]   " : "[FALLA]") + " " + que
                + " | esperado: " + esperado + " | obtenido: " + obtenido);
    }
}
