package com.example.proyect.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reserva implements Auditable {

    private static final int EDAD_MINIMA_PARA_PAGAR = 6;

    private final String codigo;
    private final LocalDate fechaEntrada;
    private final LocalDate fechaSalida;
    private final LocalDateTime fechaCreacion;
    private final long valorEstancia;
    private final Huesped titular;
    private final Apartamento apartamento;
    private final List<Ocupante> listOcupantes;
    private final Folio folio;
    private final CanalReserva canal;
    private PoliticaCancelacion politica;
    private EstadoReserva estado;
    private final List<Novedad> listNovedades = new ArrayList<>();

    public Reserva(String codigo, LocalDate fechaEntrada, LocalDate fechaSalida, long valorEstancia,
                   Huesped titular, Apartamento apartamento, List<Ocupante> ocupantes,
                   CanalReserva canal) {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("El código de la reserva es obligatorio");
        }
        if (fechaEntrada == null || fechaSalida == null || !fechaSalida.isAfter(fechaEntrada)) {
            throw new IllegalArgumentException("La salida debe ser posterior a la entrada");
        }
        if (titular == null || apartamento == null || ocupantes == null || canal == null) {
            throw new IllegalArgumentException("Titular, apartamento, ocupantes y canal son obligatorios");
        }
        this.codigo = codigo;
        this.fechaEntrada = fechaEntrada;
        this.fechaSalida = fechaSalida;
        this.fechaCreacion = LocalDateTime.now();
        this.valorEstancia = valorEstancia;
        this.titular = titular;
        this.apartamento = apartamento;
        this.listOcupantes = new ArrayList<>(ocupantes);
        this.canal = canal;
        this.folio = new Folio();
        this.estado = EstadoReserva.PENDIENTE; // toda reserva nace Pendiente
    }

    public int calcularNoches() {
        return (int) ChronoUnit.DAYS.between(fechaEntrada, fechaSalida);
    }

    /** Ocupantes de 6 años o más, con la edad calculada a la fecha de entrada. */
    public int contarOcupantesQuePagan() {
        int pagan = 0;
        for (Ocupante ocupante : listOcupantes) {
            if (ocupante.calcularEdad(fechaEntrada) >= EDAD_MINIMA_PARA_PAGAR) {
                pagan++;
            }
        }
        return pagan;
    }

    /** Ciclo de vida permitido. Cualquier otro cambio se rechaza. */
    public boolean puedeCambiarA(EstadoReserva nuevo) {
        if (nuevo == null) {
            return false;
        }
        switch (estado) {
            case PENDIENTE:
                return nuevo == EstadoReserva.CONFIRMADA || nuevo == EstadoReserva.CANCELADA;
            case CONFIRMADA:
                return nuevo == EstadoReserva.EN_CURSO || nuevo == EstadoReserva.CANCELADA
                        || nuevo == EstadoReserva.NO_SHOW;
            case EN_CURSO:
                return nuevo == EstadoReserva.FINALIZADA;
            default:
                return false; // Finalizada, Cancelada y No-show son estados finales
        }
    }

    public boolean cambiarEstado(EstadoReserva nuevo) {
        if (!puedeCambiarA(nuevo)) {
            return false;
        }
        this.estado = nuevo;
        return true;
    }

    public boolean estaActiva() {
        return estado == EstadoReserva.PENDIENTE || estado == EstadoReserva.CONFIRMADA
                || estado == EstadoReserva.EN_CURSO;
    }

    /** Cruce de noches: la noche de la fecha de salida no se ocupa. */
    public boolean seCruzaCon(LocalDate entrada, LocalDate salida) {
        return fechaEntrada.isBefore(salida) && entrada.isBefore(fechaSalida);
    }

    /** La política se congela al crear la reserva: solo se puede asignar una vez. */
    public void asignarPolitica(PoliticaCancelacion politica) {
        if (this.politica != null) {
            throw new IllegalStateException("La política ya fue congelada");
        }
        if (politica == null) {
            throw new IllegalArgumentException("La política es obligatoria");
        }
        this.politica = politica;
    }

    @Override
    public void registrarNovedad(Novedad novedad) {
        if (novedad == null) {
            throw new IllegalArgumentException("La novedad es obligatoria");
        }
        listNovedades.add(novedad);
    }

    @Override
    public List<Novedad> obtenerNovedades() {
        return Collections.unmodifiableList(listNovedades);
    }

    public String getCodigo() { return codigo; }
    public LocalDate getFechaEntrada() { return fechaEntrada; }
    public LocalDate getFechaSalida() { return fechaSalida; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public long getValorEstancia() { return valorEstancia; }
    public Huesped getTitular() { return titular; }
    public Apartamento getApartamento() { return apartamento; }
    public List<Ocupante> getOcupantes() { return Collections.unmodifiableList(listOcupantes); }
    public Folio getFolio() { return folio; }
    public CanalReserva getCanal() { return canal; }
    public PoliticaCancelacion getPolitica() { return politica; }
    public EstadoReserva getEstado() { return estado; }
}
