package com.example.proyect.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ConflictoCanal(LocalDateTime fechaRegistro, String codigoExterno,
                             String codigoApartamento, LocalDate fechaEntrada,
                             LocalDate fechaSalida, String motivo) {
}
