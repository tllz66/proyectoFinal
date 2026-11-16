package com.example.proyect.model;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Administra todas las entidades y valida las reglas del negocio.
 * Las operaciones que modifican datos devuelven true si se realizan y lanzan
 * ReglaNegocioException (con el motivo) si se rechazan, sin alterar lo ya registrado.
 */
public class Alojamiento {

    private static final LocalTime HORA_ENTRADA = LocalTime.of(15, 0);
    private static final LocalTime HORA_LIMITE_NO_SHOW = LocalTime.of(22, 0);
    private static final Duration PLAZO_CONFIRMACION = Duration.ofHours(24);

    private final String nit;
    private final String nombre;
    private final String ubicacion;
    private PoliticaCancelacion politicaVigente;

    private final List<Usuario> listUsuarios = new ArrayList<>();
    private final List<Apartamento> listApartamentos = new ArrayList<>();
    private final List<Reserva> listReservas = new ArrayList<>();
    private final List<Bloqueo> listBloqueos = new ArrayList<>();
    private final List<PeriodoTemporada> listTemporadas = new ArrayList<>();
    private final List<ConflictoCanal> listConflictos = new ArrayList<>();

    public Alojamiento(String nit, String nombre, String ubicacion) {
        this.nit = nit;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.politicaVigente = new PoliticaEscalonada(168, 48, 0.50); // valores del enunciado
    }

    // ===================== USUARIOS =====================

    public boolean registrarUsuario(Usuario usuario) {
        if (usuario == null) {
            rechazar("El usuario es obligatorio");
        }
        if (buscarUsuario(usuario.getDocumento()) != null) {
            rechazar("Ya existe un usuario con el documento " + usuario.getDocumento());
        }
        for (Usuario existente : listUsuarios) {
            if (existente.getUsuario().equals(usuario.getUsuario())) {
                rechazar("El nombre de usuario '" + usuario.getUsuario() + "' ya está en uso");
            }
        }
        listUsuarios.add(usuario);
        return true;
    }

    /** Devuelve el usuario autenticado, o null si las credenciales no son correctas. */
    public Usuario iniciarSesion(String usuario, String contrasena) {
        for (Usuario u : listUsuarios) {
            if (u.autenticar(usuario, contrasena)) {
                return u;
            }
        }
        return null;
    }

    public boolean desvincularUsuario(String documento) {
        Usuario u = buscarUsuario(documento);
        if (u == null) {
            rechazar("No existe un usuario con el documento " + documento);
        }
        if (!u.isActivo()) {
            rechazar("El usuario ya está desvinculado");
        }
        u.desactivar();
        return true;
    }

    public Usuario buscarUsuario(String documento) {
        for (Usuario u : listUsuarios) {
            if (u.getDocumento().equals(documento)) {
                return u;
            }
        }
        return null;
    }

    // ===================== APARTAMENTOS, BLOQUEOS, TEMPORADAS, POLÍTICA =====================

    public boolean registrarApartamento(Apartamento apartamento) {
        if (apartamento == null) {
            rechazar("El apartamento es obligatorio");
        }
        if (buscarApartamento(apartamento.getCodigo()) != null) {
            rechazar("Ya existe un apartamento con el código " + apartamento.getCodigo());
        }
        listApartamentos.add(apartamento);
        return true;
    }

    /** Eliminación lógica: se rechaza si tiene reservas activas. */
    public boolean eliminarApartamento(String codigo) {
        Apartamento a = exigirApartamento(codigo);
        if (!a.isActivo()) {
            rechazar("El apartamento ya fue eliminado");
        }
        for (Reserva r : listReservas) {
            if (r.getApartamento().getCodigo().equals(codigo) && r.estaActiva()) {
                rechazar("No se puede eliminar: tiene la reserva activa " + r.getCodigo());
            }
        }
        a.desactivar();
        return true;
    }

    public boolean registrarBloqueo(Bloqueo bloqueo) {
        if (bloqueo == null) {
            rechazar("El bloqueo es obligatorio");
        }
        exigirApartamento(bloqueo.codigoApartamento());
        if (hayCruceConReservas(bloqueo.codigoApartamento(), bloqueo.fechaInicio(), bloqueo.fechaFin())) {
            rechazar("El bloqueo choca con una reserva activa del apartamento");
        }
        listBloqueos.add(bloqueo);
        return true;
    }

    public boolean definirTemporada(PeriodoTemporada periodo) {
        if (periodo == null) {
            rechazar("El periodo es obligatorio");
        }
        for (PeriodoTemporada existente : listTemporadas) {
            boolean seCruzan = !periodo.fechaInicio().isAfter(existente.fechaFin())
                    && !existente.fechaInicio().isAfter(periodo.fechaFin());
            if (seCruzan) {
                rechazar("El periodo se cruza con otro de temporada " + existente.temporada()
                        + " (" + existente.fechaInicio() + " a " + existente.fechaFin() + ")");
            }
        }
        listTemporadas.add(periodo);
        return true;
    }

    /** Solo afecta a las reservas que se creen después: las existentes conservan la suya. */
    public void configurarPolitica(PoliticaCancelacion politica) {
        if (politica == null) {
            rechazar("La política es obligatoria");
        }
        this.politicaVigente = politica;
    }

    public boolean cambiarEstadoApartamento(String codigo, EstadoApartamento nuevo, String motivo) {
        Apartamento a = exigirApartamento(codigo);
        if (nuevo == null) {
            rechazar("El estado es obligatorio");
        }
        if (nuevo == EstadoApartamento.FUERA_DE_SERVICIO) {
            if (motivo == null || motivo.isBlank()) {
                rechazar("Fuera de servicio exige registrar el motivo");
            }
            a.setMotivoFueraServicio(motivo);
        }
        a.cambiarEstado(nuevo);
        return true;
    }

    public boolean registrarNovedad(String codigoApartamento, Novedad novedad) {
        Apartamento a = exigirApartamento(codigoApartamento);
        if (novedad == null) {
            rechazar("La novedad es obligatoria");
        }
        a.registrarNovedad(novedad);
        return true;
    }

    // ===================== DISPONIBILIDAD Y COTIZACIÓN =====================

    public List<Apartamento> consultarDisponibles(LocalDate entrada, LocalDate salida, int personas) {
        validarFechas(entrada, salida);
        if (personas <= 0) {
            rechazar("El número de personas debe ser mayor que cero");
        }
        List<Apartamento> disponibles = new ArrayList<>();
        for (Apartamento a : listApartamentos) {
            if (a.isActivo() && a.getCapacidad() >= personas
                    && estaDisponible(a.getCodigo(), entrada, salida)) {
                disponibles.add(a);
            }
        }
        return disponibles;
    }

    /** Valor de cada noche: tarifa de esa temporada x ocupantes de 6 años o más. */
    public List<Long> desglosarEstancia(String codigoApartamento, LocalDate entrada,
                                        LocalDate salida, List<Ocupante> ocupantes) {
        Apartamento a = exigirApartamento(codigoApartamento);
        validarFechas(entrada, salida);
        if (ocupantes == null || ocupantes.isEmpty()) {
            rechazar("Debe registrar al menos un ocupante");
        }
        int pagan = 0;
        for (Ocupante o : ocupantes) {
            if (o.calcularEdad(entrada) >= 6) {
                pagan++;
            }
        }
        List<Long> desglose = new ArrayList<>();
        for (LocalDate noche = entrada; noche.isBefore(salida); noche = noche.plusDays(1)) {
            desglose.add(a.getTarifa(temporadaDe(noche)) * pagan);
        }
        return desglose;
    }

    public long cotizarEstancia(String codigoApartamento, LocalDate entrada, LocalDate salida,
                                List<Ocupante> ocupantes) {
        long total = 0;
        for (long noche : desglosarEstancia(codigoApartamento, entrada, salida, ocupantes)) {
            total += noche;
        }
        return total;
    }

    public Temporada temporadaDe(LocalDate dia) {
        for (PeriodoTemporada p : listTemporadas) {
            if (p.contiene(dia)) {
                return p.temporada();
            }
        }
        return Temporada.BAJA; // temporada base
    }

    // ===================== RESERVAS =====================

    public boolean crearReserva(Reserva reserva) {
        validarReserva(reserva);
        reserva.asignarPolitica(politicaVigente); // se congela al crear
        reserva.getFolio().agregarCargo(new CargoAlojamiento(
                "Alojamiento reserva " + reserva.getCodigo(), LocalDate.now(),
                reserva.calcularNoches(), reserva.getValorEstancia()));
        listReservas.add(reserva);
        return true;
    }

    public boolean recibirReservaExterna(Reserva reserva) {
        if (reserva == null || reserva.getCanal().getTipo() != TipoCanal.EXTERNA) {
            rechazar("La reserva debe venir del canal externo");
        }
        String codigoExterno = ((CanalExterno) reserva.getCanal()).getCodigoExterno();
        String codigoApto = reserva.getApartamento().getCodigo();

        if (existeCodigoExterno(codigoExterno)) {
            registrarConflicto(reserva, codigoExterno, "Código externo repetido");
            rechazar("El código externo " + codigoExterno + " ya creó una reserva (conflicto registrado)");
        }
        if (hayCruceConReservas(codigoApto, reserva.getFechaEntrada(), reserva.getFechaSalida())) {
            registrarConflicto(reserva, codigoExterno, "Choca con una reserva vigente");
            rechazar("Choca con una reserva vigente: se rechazó y se registró como conflicto de canal");
        }
        return crearReserva(reserva);
    }

    public boolean confirmarReserva(String codigoReserva) {
        Reserva r = exigirReserva(codigoReserva);
        if (r.getEstado() != EstadoReserva.PENDIENTE) {
            rechazar("Solo se confirman reservas Pendientes (esta está " + r.getEstado() + ")");
        }
        long anticipo = r.getCanal().calcularAnticipo(r.getValorEstancia());
        if (r.getCanal().exigeAnticipo() && r.getFolio().calcularTotalPagos() < anticipo) {
            rechazar("Falta pagar el anticipo de " + dinero(anticipo));
        }
        r.cambiarEstado(EstadoReserva.CONFIRMADA);
        return true;
    }

    public boolean cancelarReserva(String codigoReserva) {
        Reserva r = exigirReserva(codigoReserva);
        exigirCancelable(r);
        cancelar(r, LocalDateTime.now());
        return true;
    }

    /** Cuánto se le devolvería al huésped si cancelara en el momento "ahora". */
    public long calcularDevolucion(String codigoReserva, LocalDateTime ahora) {
        Reserva r = exigirReserva(codigoReserva);
        exigirCancelable(r);
        return reembolso(r, ahora);
    }

    /** Cancela las Pendientes con 24 horas o más sin confirmar. Devuelve cuántas canceló. */
    public int cancelarPendientesVencidas(LocalDateTime ahora) {
        int canceladas = 0;
        for (Reserva r : listReservas) {
            boolean vencida = !r.getFechaCreacion().plus(PLAZO_CONFIRMACION).isAfter(ahora);
            if (r.getEstado() == EstadoReserva.PENDIENTE && vencida) {
                cancelar(r, ahora);
                canceladas++;
            }
        }
        return canceladas;
    }

    public boolean hacerCheckIn(String codigoReserva) {
        Reserva r = exigirReserva(codigoReserva);
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            rechazar("El check-in exige reserva Confirmada (esta está " + r.getEstado() + ")");
        }
        if (LocalDate.now().isBefore(r.getFechaEntrada())) {
            rechazar("Aún no llega la fecha de entrada (" + r.getFechaEntrada() + ")");
        }
        if (r.getApartamento().getEstado() != EstadoApartamento.PREPARADO) {
            rechazar("El apartamento no está Preparado (está " + r.getApartamento().getEstado() + ")");
        }
        r.cambiarEstado(EstadoReserva.EN_CURSO);
        r.getApartamento().cambiarEstado(EstadoApartamento.OCUPADO);
        return true;
    }

    public boolean hacerCheckOut(String codigoReserva) {
        Reserva r = exigirReserva(codigoReserva);
        if (r.getEstado() != EstadoReserva.EN_CURSO) {
            rechazar("El check-out exige reserva En curso (esta está " + r.getEstado() + ")");
        }
        r.cambiarEstado(EstadoReserva.FINALIZADA);
        r.getApartamento().cambiarEstado(EstadoApartamento.PENDIENTE_PREPARACION);
        return true;
    }

    public boolean declararNoShow(String codigoReserva) {
        Reserva r = exigirReserva(codigoReserva);
        if (r.getEstado() != EstadoReserva.CONFIRMADA) {
            rechazar("El no-show exige reserva Confirmada (esta está " + r.getEstado() + ")");
        }
        if (LocalDateTime.now().isBefore(r.getFechaEntrada().atTime(HORA_LIMITE_NO_SHOW))) {
            rechazar("El no-show solo se declara desde las 10:00 p. m. del día de entrada");
        }
        r.cambiarEstado(EstadoReserva.NO_SHOW); // retiene el 100 %: no hay devolución
        return true;
    }

    // ===================== FOLIO =====================

    public boolean registrarPago(String codigoReserva, Pago pago) {
        Reserva r = exigirReserva(codigoReserva);
        if (pago == null) {
            rechazar("El pago es obligatorio");
        }
        r.getFolio().agregarPago(pago);
        // Una reserva Pendiente que exige anticipo pasa a Confirmada cuando el anticipo queda cubierto
        if (r.getEstado() == EstadoReserva.PENDIENTE && r.getCanal().exigeAnticipo()
                && r.getFolio().calcularTotalPagos() >= r.getCanal().calcularAnticipo(r.getValorEstancia())) {
            r.cambiarEstado(EstadoReserva.CONFIRMADA);
        }
        return true;
    }

    public boolean registrarCargo(String codigoReserva, Cargo cargo) {
        Reserva r = exigirReserva(codigoReserva);
        if (cargo == null) {
            rechazar("El cargo es obligatorio");
        }
        r.getFolio().agregarCargo(cargo);
        return true;
    }

    // ===================== REPORTES =====================

    /** Pagos menos devoluciones del periodo, sin descontar la comisión externa. */
    public long calcularIngresosPorPeriodo(LocalDate inicio, LocalDate fin) {
        long total = 0;
        for (Reserva r : listReservas) {
            for (Pago p : r.getFolio().getPagos()) {
                if (!p.fecha().isBefore(inicio) && !p.fecha().isAfter(fin)) {
                    total += p.monto();
                }
            }
        }
        return total;
    }

    public List<String> reporteOcupacion(LocalDate inicio, LocalDate fin) {
        List<String> filas = new ArrayList<>();
        long noches = ChronoUnit.DAYS.between(inicio, fin);
        for (Apartamento a : listApartamentos) {
            long ocupadas = 0;
            for (Reserva r : listReservas) {
                boolean cuenta = r.getEstado() == EstadoReserva.CONFIRMADA
                        || r.getEstado() == EstadoReserva.EN_CURSO
                        || r.getEstado() == EstadoReserva.FINALIZADA;
                if (cuenta && r.getApartamento().getCodigo().equals(a.getCodigo())) {
                    LocalDate desde = r.getFechaEntrada().isAfter(inicio) ? r.getFechaEntrada() : inicio;
                    LocalDate hasta = r.getFechaSalida().isBefore(fin) ? r.getFechaSalida() : fin;
                    ocupadas += Math.max(0, ChronoUnit.DAYS.between(desde, hasta));
                }
            }
            double porcentaje = noches > 0 ? ocupadas * 100.0 / noches : 0;
            filas.add(String.format("%s - %s: %d de %d noches (%.1f%%)",
                    a.getCodigo(), a.getNombre(), ocupadas, noches, porcentaje));
        }
        return filas;
    }

    /** Incluye las canceladas y los no-show. */
    public List<String> reporteReservasPorCanal() {
        List<String> filas = new ArrayList<>();
        for (TipoCanal tipo : TipoCanal.values()) {
            int total = 0, canceladas = 0, noShow = 0;
            for (Reserva r : listReservas) {
                if (r.getCanal().getTipo() == tipo) {
                    total++;
                    if (r.getEstado() == EstadoReserva.CANCELADA) canceladas++;
                    if (r.getEstado() == EstadoReserva.NO_SHOW) noShow++;
                }
            }
            filas.add(tipo + ": " + total + " reservas (canceladas: " + canceladas
                    + ", no-show: " + noShow + ")");
        }
        return filas;
    }

    /** Excluye Canceladas y No-show: su cuenta queda cerrada. */
    public List<Reserva> reporteSaldosPendientes() {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : listReservas) {
            boolean cerrada = r.getEstado() == EstadoReserva.CANCELADA
                    || r.getEstado() == EstadoReserva.NO_SHOW;
            if (!cerrada && r.getFolio().calcularSaldo() > 0) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public List<Apartamento> reporteApartamentosConMasNovedades() {
        List<Apartamento> resultado = new ArrayList<>();
        for (Apartamento a : listApartamentos) {
            if (!a.obtenerNovedades().isEmpty()) {
                resultado.add(a);
            }
        }
        resultado.sort((x, y) -> y.obtenerNovedades().size() - x.obtenerNovedades().size());
        return resultado;
    }

    // ===================== BÚSQUEDAS Y CONSULTAS =====================

    public Apartamento buscarApartamento(String codigo) {
        for (Apartamento a : listApartamentos) {
            if (a.getCodigo().equals(codigo)) {
                return a;
            }
        }
        return null;
    }

    public Reserva buscarReserva(String codigo) {
        for (Reserva r : listReservas) {
            if (r.getCodigo().equals(codigo)) {
                return r;
            }
        }
        return null;
    }

    public List<Reserva> consultarReservasDeHuesped(String documento) {
        List<Reserva> resultado = new ArrayList<>();
        for (Reserva r : listReservas) {
            if (r.getTitular().getDocumento().equals(documento)) {
                resultado.add(r);
            }
        }
        return resultado;
    }

    public String getNit() { return nit; }
    public String getNombre() { return nombre; }
    public String getUbicacion() { return ubicacion; }
    public PoliticaCancelacion getPoliticaVigente() { return politicaVigente; }
    public List<Usuario> getUsuarios() { return Collections.unmodifiableList(listUsuarios); }
    public List<Apartamento> getApartamentos() { return Collections.unmodifiableList(listApartamentos); }
    public List<Reserva> getReservas() { return Collections.unmodifiableList(listReservas); }
    public List<Bloqueo> getBloqueos() { return Collections.unmodifiableList(listBloqueos); }
    public List<PeriodoTemporada> getTemporadas() { return Collections.unmodifiableList(listTemporadas); }
    public List<ConflictoCanal> getConflictos() { return Collections.unmodifiableList(listConflictos); }

    // ===================== MÉTODOS PRIVADOS =====================

    private static void rechazar(String motivo) {
        throw new ReglaNegocioException(motivo);
    }

    private static String dinero(long valor) {
        return String.format("$%,d", valor);
    }

    private Apartamento exigirApartamento(String codigo) {
        Apartamento a = buscarApartamento(codigo);
        if (a == null) {
            rechazar("No existe el apartamento " + codigo);
        }
        return a;
    }

    private Reserva exigirReserva(String codigo) {
        Reserva r = buscarReserva(codigo);
        if (r == null) {
            rechazar("No existe la reserva " + codigo);
        }
        return r;
    }

    private void exigirCancelable(Reserva r) {
        if (!r.puedeCambiarA(EstadoReserva.CANCELADA)) {
            rechazar("Una reserva " + r.getEstado() + " no se puede cancelar");
        }
    }

    private static void validarFechas(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            rechazar("Las fechas de entrada y salida son obligatorias");
        }
        if (!salida.isAfter(entrada)) {
            rechazar("La salida debe ser posterior a la entrada (mínimo una noche)");
        }
    }

    private void validarReserva(Reserva r) {
        if (r == null) {
            rechazar("La reserva es obligatoria");
        }
        if (buscarReserva(r.getCodigo()) != null) {
            rechazar("Ya existe una reserva con el código " + r.getCodigo());
        }
        Apartamento apto = exigirApartamento(r.getApartamento().getCodigo());
        if (!apto.isActivo()) {
            rechazar("El apartamento " + apto.getCodigo() + " fue eliminado");
        }
        if (buscarUsuario(r.getTitular().getDocumento()) == null) {
            rechazar("El titular no está registrado");
        }
        if (r.getFechaEntrada().isBefore(LocalDate.now())) {
            rechazar("No se aceptan fechas de entrada anteriores a hoy");
        }
        if (r.getOcupantes().isEmpty()) {
            rechazar("Debe registrar al menos un ocupante");
        }
        if (r.getOcupantes().size() > apto.getCapacidad()) {
            rechazar("Supera la capacidad del apartamento (" + apto.getCapacidad()
                    + " personas, incluidos los menores)");
        }
        if (hayCruceConReservas(apto.getCodigo(), r.getFechaEntrada(), r.getFechaSalida())) {
            rechazar("Doble venta: el apartamento ya tiene una reserva activa en esas noches");
        }
        for (Bloqueo b : listBloqueos) {
            if (b.codigoApartamento().equals(apto.getCodigo())
                    && b.seCruzaCon(r.getFechaEntrada(), r.getFechaSalida())) {
                rechazar("El apartamento está bloqueado en esas noches (" + b.motivo() + ")");
            }
        }
        long valor = cotizarEstancia(apto.getCodigo(), r.getFechaEntrada(), r.getFechaSalida(),
                r.getOcupantes());
        if (valor != r.getValorEstancia()) {
            rechazar("El valor de la estancia no coincide con el calculado (" + dinero(valor) + ")");
        }
    }

    private boolean estaDisponible(String codigoApto, LocalDate entrada, LocalDate salida) {
        if (hayCruceConReservas(codigoApto, entrada, salida)) {
            return false;
        }
        for (Bloqueo b : listBloqueos) {
            if (b.codigoApartamento().equals(codigoApto) && b.seCruzaCon(entrada, salida)) {
                return false;
            }
        }
        return true;
    }

    /** Regla más importante: sin doble venta, sin importar el canal. */
    private boolean hayCruceConReservas(String codigoApto, LocalDate entrada, LocalDate salida) {
        for (Reserva r : listReservas) {
            if (r.estaActiva() && r.getApartamento().getCodigo().equals(codigoApto)
                    && r.seCruzaCon(entrada, salida)) {
                return true;
            }
        }
        return false;
    }

    private boolean existeCodigoExterno(String codigoExterno) {
        for (Reserva r : listReservas) {
            if (r.getCanal().getTipo() == TipoCanal.EXTERNA
                    && ((CanalExterno) r.getCanal()).getCodigoExterno().equals(codigoExterno)) {
                return true;
            }
        }
        return false;
    }

    private void registrarConflicto(Reserva r, String codigoExterno, String motivo) {
        listConflictos.add(new ConflictoCanal(LocalDateTime.now(), codigoExterno,
                r.getApartamento().getCodigo(), r.getFechaEntrada(), r.getFechaSalida(), motivo));
    }

    private long reembolso(Reserva r, LocalDateTime ahora) {
        long minutos = Duration.between(ahora, r.getFechaEntrada().atTime(HORA_ENTRADA)).toMinutes();
        long horas = Math.floorDiv(minutos, 60);
        return r.getPolitica().calcularReembolso(r.getValorEstancia(),
                r.getFolio().calcularTotalPagos(), horas);
    }

    private void cancelar(Reserva r, LocalDateTime ahora) {
        long devolucion = reembolso(r, ahora);
        if (devolucion > 0) {
            r.getFolio().agregarPago(new Pago(-devolucion, MedioPago.EFECTIVO, ahora.toLocalDate()));
        }
        r.cambiarEstado(EstadoReserva.CANCELADA);
    }
}
