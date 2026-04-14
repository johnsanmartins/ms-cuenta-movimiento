package com.bank.cuentamovimiento.service;

import com.bank.cuentamovimiento.dto.MovimientoDTO;
import com.bank.cuentamovimiento.dto.ReporteEstadoCuentaDTO;

import java.time.LocalDate;
import java.util.List;

public interface MovimientoService {

    MovimientoDTO registrar(MovimientoDTO movimientoDTO);

    MovimientoDTO obtenerPorId(Long id);

    List<MovimientoDTO> obtenerTodos();

    List<MovimientoDTO> obtenerPorCuenta(String numeroCuenta);

    MovimientoDTO actualizar(Long id, MovimientoDTO movimientoDTO);

    ReporteEstadoCuentaDTO generarReporte(Long clienteId, String clienteNombre, LocalDate fechaInicio, LocalDate fechaFin);
}
