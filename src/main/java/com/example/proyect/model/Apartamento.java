package com.example.proyect.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Apartamento implements Auditable {

    private final String codigo;
    private String nombre;
    private final int dormitorios;
    private final int capacidad;
    private String caracteristicas;
    private long tarifaBaja;
    private long tarifaMedia;
    private long tarifaAlta;
    private EstadoApartamento estado;
    private String motivoFueraServicio;
    private boolean activo;
    private final List<Novedad> listNovedades;

    public Apartamento(String codigo, String nombre, int dormitorios, int capacidad,
                       String caracteristicas, long tarifaBaja, long tarifaMedia, long tarifaAlta) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("Código y nombre son obligatorios");
        }
        if (dormitorios <= 0 || capacidad <= 0) {
            throw new IllegalArgumentException("Dormitorios y capacidad deben ser positivos");
        }
        if (tarifaBaja <= 0 || tarifaMedia <= 0 || tarifaAlta <= 0) {
            throw new IllegalArgumentException("Las tarifas deben ser positivas");
        }
        this.codigo = codigo;
        this.nombre = nombre;
        this.dormitorios = dormitorios;
        this.capacidad = capacidad;
        this.caracteristicas = caracteristicas == null ? "" : caracteristicas;
        this.tarifaBaja = tarifaBaja;
        this.tarifaMedia = tarifaMedia;
        this.tarifaAlta = tarifaAlta;
        this.estado = EstadoApartamento.PREPARADO;
        this.activo = true;
        this.listNovedades = new ArrayList<>();
    }

    /** Tarifa por persona por noche de la temporada indicada. */
    public long getTarifa(Temporada temporada) {
        switch (temporada) {
            case MEDIA: return tarifaMedia;
            case ALTA: return tarifaAlta;
            default: return tarifaBaja;
        }
    }

    /** Si el nuevo estado es FUERA_DE_SERVICIO, Alojamiento debe fijar antes el motivo. */
    public void cambiarEstado(EstadoApartamento nuevo) {
        if (nuevo == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        this.estado = nuevo;
        if (nuevo != EstadoApartamento.FUERA_DE_SERVICIO) {
            this.motivoFueraServicio = null;
        }
    }

    public void setMotivoFueraServicio(String motivo) {
        this.motivoFueraServicio = motivo;
    }

    public void desactivar() {
        this.activo = false;
    }

    public void setTarifas(long baja, long media, long alta) {
        if (baja <= 0 || media <= 0 || alta <= 0) {
            throw new IllegalArgumentException("Las tarifas deben ser positivas");
        }
        this.tarifaBaja = baja;
        this.tarifaMedia = media;
        this.tarifaAlta = alta;
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
    public String getNombre() { return nombre; }
    public int getDormitorios() { return dormitorios; }
    public int getCapacidad() { return capacidad; }
    public String getCaracteristicas() { return caracteristicas; }
    public EstadoApartamento getEstado() { return estado; }
    public String getMotivoFueraServicio() { return motivoFueraServicio; }
    public boolean isActivo() { return activo; }
}
