package com.bank.cuentamovimiento.controller;

import com.bank.cuentamovimiento.dto.ReporteEstadoCuentaDTO;
import com.bank.cuentamovimiento.service.MovimientoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final MovimientoService movimientoService;

    @GetMapping
    public ResponseEntity<ReporteEstadoCuentaDTO> generarEstadoCuenta(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate fechaFin,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(required = false) String cliente) {

        if (fechaInicio == null) {
            fechaInicio = LocalDate.now().minusMonths(1);
        }
        if (fechaFin == null) {
            fechaFin = LocalDate.now();
        }

        ReporteEstadoCuentaDTO reporte = movimientoService.generarReporte(clienteId, cliente, fechaInicio, fechaFin);
        return ResponseEntity.ok(reporte);
    }
}
