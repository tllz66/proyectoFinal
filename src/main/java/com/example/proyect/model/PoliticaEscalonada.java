package com.example.proyect.model;


/**
 * Más de horasReembolsoTotal: se devuelve todo lo pagado.
 * Entre horasRetencionParcial y horasReembolsoTotal: se retiene un porcentaje de la estancia.
 * Con menos de horasRetencionParcial: se retiene el 100 %.
 * Valores del enunciado: (168, 48, 0.50). Las horas se cuentan completas (se truncan).
 */
public class PoliticaEscalonada implements PoliticaCancelacion {

    private final long horasReembolsoTotal;
    private final long horasRetencionParcial;
    private final double porcentajeRetencionParcial;

    public PoliticaEscalonada(long horasReembolsoTotal, long horasRetencionParcial,
                              double porcentajeRetencionParcial) {
        if (horasRetencionParcial < 0 || horasReembolsoTotal < horasRetencionParcial) {
            throw new IllegalArgumentException("Los plazos de la política no son válidos");
        }
        if (porcentajeRetencionParcial < 0 || porcentajeRetencionParcial > 1) {
            throw new IllegalArgumentException("El porcentaje debe estar entre 0 y 1");
        }
        this.horasReembolsoTotal = horasReembolsoTotal;
        this.horasRetencionParcial = horasRetencionParcial;
        this.porcentajeRetencionParcial = porcentajeRetencionParcial;
    }

    @Override
    public long calcularReembolso(long valorEstancia, long totalPagado, long horasAntelacion) {
        if (horasAntelacion > horasReembolsoTotal) {
            return Math.max(0, totalPagado);
        }
        if (horasAntelacion >= horasRetencionParcial) {
            long retenido = Math.round(valorEstancia * porcentajeRetencionParcial);
            return Math.max(0, totalPagado - retenido);
        }
        return 0;
    }
}
