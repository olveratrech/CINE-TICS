package com.penta.cinetics.web;

import com.penta.cinetics.identidad.IdentidadesOracle;
import com.penta.cinetics.reservas.aplicacion.Reservas;
import com.penta.cinetics.boletos.aplicacion.VentaBoletos;
import java.sql.SQLException;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@RestControllerAdvice
public class ErroresApi {
    private ResponseEntity<Map<String,String>> error(int status,String code) { return ResponseEntity.status(status).body(Map.of("code",code)); }
    @ExceptionHandler({IllegalArgumentException.class,ArithmeticException.class,MethodArgumentNotValidException.class,HttpMessageNotReadableException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class})
    ResponseEntity<Map<String,String>> invalid(Exception ignored) { return error(400,"INVALID_REQUEST"); }
    @ExceptionHandler(IdentidadesOracle.Duplicado.class)
    ResponseEntity<Map<String,String>> duplicate() { return error(409,"REGISTRATION_CONFLICT"); }
    @ExceptionHandler(Reservas.Rechazo.class)
    ResponseEntity<Map<String,String>> hold(Reservas.Rechazo rejected) { return error(rejected.motivo()==Reservas.Motivo.NO_EXISTE?404:409,rejected.motivo().name()); }
    @ExceptionHandler(VentaBoletos.Rechazo.class)
    ResponseEntity<Map<String,String>> payment(VentaBoletos.Rechazo rejected) { return error(rejected.motivo()==VentaBoletos.Motivo.NO_EXISTE?404:409,rejected.motivo().name()); }
    @ExceptionHandler(SQLException.class)
    ResponseEntity<Map<String,String>> database(SQLException ignored) { return error(503,"DATABASE_UNAVAILABLE_RETRY_SAME_KEY"); }
}
