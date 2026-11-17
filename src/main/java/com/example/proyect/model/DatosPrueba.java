package com.example.proyect.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Datos para demostraciones.
 *
 * Usuarios (usuario / contraseña):
 *   stellez / Admin#2026        (Administrador)
 *   camila / Recep#2026         (Recepcionista)
 *   valentina / Valen#2026, andres / Andres#2026, daniela / Dani#2026   (Huéspedes)
 *
 * Reserva RES-HOY: Confirmada con entrada hoy en A103, lista para hacer check-in.
 */
public class DatosPrueba {

    public static Alojamiento crearAlojamientoConDatos() {
        Alojamiento aloj = new Alojamiento("900123456-7", "Aparta-Suites UQ", "Salento, Quindío");
        cargar(aloj);
        return aloj;
    }

    public static void cargar(Alojamiento aloj) {
        LocalDate hoy = LocalDate.now();

        // Usuarios
        aloj.registrarUsuario(new Administrador("Santiago Téllez", "9000", "stellez@aparta.co", "3000000000",
                "stellez", "Admin#2026"));
        aloj.registrarUsuario(new Recepcionista("Jhan Martinez", "8001", "jhan.martinez@aparta.co", "3001111111",
                "jhan", "Recep#2026", hoy.minusYears(2)));
        Huesped ana = new Huesped("Valentina Mora", "1001", "valentina.mora@mail.com", "3102222222",
                "valentina", "Valen#2026", LocalDate.of(1990, 5, 14));
        Huesped carlos = new Huesped("Andrés Cruz", "1002", "andres.cruz@mail.com", "3203333333",
                "andres", "Andres#2026", LocalDate.of(1985, 9, 2));
        Huesped laura = new Huesped("Daniela Vargas", "1003", "daniela.vargas@mail.com", "3154444444",
                "daniela", "Dani#2026", LocalDate.of(1998, 1, 27));
        aloj.registrarUsuario(ana);
        aloj.registrarUsuario(carlos);
        aloj.registrarUsuario(laura);

        // Apartamentos (tarifa por persona por noche: baja, media, alta)
        aloj.registrarApartamento(new Apartamento("A101", "Cafetal", 1, 2,
                "Balcón, vista a las montañas", 90000, 120000, 160000));
        aloj.registrarApartamento(new Apartamento("A102", "Palma de Cera", 2, 4,
                "Chimenea, cocina completa", 80000, 105000, 140000));
        aloj.registrarApartamento(new Apartamento("A103", "Cocora", 2, 4,
                "Jacuzzi, terraza privada", 100000, 130000, 180000));
        aloj.registrarApartamento(new Apartamento("A104", "Las Brisas", 3, 6,
                "Zona social amplia, chimenea", 75000, 100000, 130000));

        // Temporadas (Baja es la base y no se registra)
        int anio = hoy.getYear();
        aloj.definirTemporada(new PeriodoTemporada(Temporada.MEDIA,
                LocalDate.of(anio, 12, 6), LocalDate.of(anio, 12, 9)));
        aloj.definirTemporada(new PeriodoTemporada(Temporada.ALTA,
                LocalDate.of(anio, 12, 15), LocalDate.of(anio + 1, 1, 15)));

        // Novedad de ejemplo
        aloj.registrarNovedad("A101", new Novedad(LocalDateTime.now(), TipoNovedad.FALTANTE,
                "Falta una toalla de baño"));

        // Reserva Directa confirmada, para mostrar el cálculo y los reportes
        reservar(aloj, "RES-001", "A101", ana, hoy.plusDays(5), hoy.plusDays(7),
                List.of(new Ocupante("Valentina Mora", LocalDate.of(1990, 5, 14)),
                        new Ocupante("Sergio Mora", LocalDate.of(1988, 3, 3))),
                new CanalDirecto());
        aloj.confirmarReserva("RES-001");

        // Reserva Portal con anticipo pagado (el anticipo la confirma). Incluye un menor de 6 años.
        reservar(aloj, "RES-002", "A102", carlos, hoy.plusDays(10), hoy.plusDays(13),
                List.of(new Ocupante("Andrés Cruz", LocalDate.of(1985, 9, 2)),
                        new Ocupante("Lucía Cruz", LocalDate.of(1987, 6, 20)),
                        new Ocupante("Mateo Cruz", hoy.minusYears(3))),
                new CanalPortal());
        long anticipo = aloj.buscarReserva("RES-002").getCanal()
                .calcularAnticipo(aloj.buscarReserva("RES-002").getValorEstancia());
        aloj.registrarPago("RES-002", new Pago(anticipo, MedioPago.TRANSFERENCIA, hoy));

        // Reserva Portal sin pagar: queda Pendiente
        reservar(aloj, "RES-003", "A104", laura, hoy.plusDays(20), hoy.plusDays(22),
                List.of(new Ocupante("Daniela Vargas", LocalDate.of(1998, 1, 27))),
                new CanalPortal());

        // Reserva Externa
        reservar(aloj, "RES-EXT-1", "A101", carlos, hoy.plusDays(15), hoy.plusDays(16),
                List.of(new Ocupante("Andrés Cruz", LocalDate.of(1985, 9, 2))),
                new CanalExterno("BKG-778123"));

        // Reserva que entra hoy, ya confirmada: sirve para demostrar el check-in
        reservar(aloj, "RES-HOY", "A103", ana, hoy, hoy.plusDays(2),
                List.of(new Ocupante("Valentina Mora", LocalDate.of(1990, 5, 14)),
                        new Ocupante("Sergio Mora", LocalDate.of(1988, 3, 3))),
                new CanalDirecto());
        aloj.confirmarReserva("RES-HOY");
    }

    private static void reservar(Alojamiento aloj, String codigo, String codigoApto, Huesped titular,
                                 LocalDate entrada, LocalDate salida, List<Ocupante> ocupantes,
                                 CanalReserva canal) {
        long valor = aloj.cotizarEstancia(codigoApto, entrada, salida, ocupantes);
        Reserva r = new Reserva(codigo, entrada, salida, valor, titular,
                aloj.buscarApartamento(codigoApto), ocupantes, canal);
        if (canal instanceof CanalExterno) {
            aloj.recibirReservaExterna(r);
        } else {
            aloj.crearReserva(r);
        }
    }
}
