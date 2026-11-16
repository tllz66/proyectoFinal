package com.example.proyect.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Folio {

    private final List<Cargo> listCargos = new ArrayList<>();
    private final List<Pago> listPagos = new ArrayList<>();

    public void agregarCargo(Cargo cargo) {
        if (cargo == null) {
            throw new IllegalArgumentException("El cargo es obligatorio");
        }
        listCargos.add(cargo);
    }

    public void agregarPago(Pago pago) {
        if (pago == null) {
            throw new IllegalArgumentException("El pago es obligatorio");
        }
        listPagos.add(pago);
    }

    public long calcularTotalCargos() {
        long total = 0;
        for (Cargo cargo : listCargos) {
            total += cargo.calcularValor(); // polimorfismo
        }
        return total;
    }

    public long calcularTotalPagos() {
        long total = 0;
        for (Pago pago : listPagos) {
            total += pago.monto();
        }
        return total;
    }

    public long calcularSaldo() {
        return calcularTotalCargos() - calcularTotalPagos();
    }

    public List<Cargo> getCargos() {
        return Collections.unmodifiableList(listCargos);
    }

    public List<Pago> getPagos() {
        return Collections.unmodifiableList(listPagos);
    }
}
