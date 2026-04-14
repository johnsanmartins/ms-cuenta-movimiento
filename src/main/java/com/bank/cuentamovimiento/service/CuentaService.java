package com.bank.cuentamovimiento.service;

import com.bank.cuentamovimiento.dto.CuentaDTO;

import java.util.List;

public interface CuentaService {

    CuentaDTO crear(CuentaDTO cuentaDTO);

    CuentaDTO obtenerPorId(Long id);

    CuentaDTO obtenerPorNumeroCuenta(String numeroCuenta);

    List<CuentaDTO> obtenerTodas();

    List<CuentaDTO> obtenerPorClienteId(Long clienteId);

    CuentaDTO actualizar(Long id, CuentaDTO cuentaDTO);
}
