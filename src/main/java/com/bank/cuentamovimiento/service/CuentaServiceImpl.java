package com.bank.cuentamovimiento.service;

import com.bank.cuentamovimiento.dto.CuentaDTO;
import com.bank.cuentamovimiento.entity.Cuenta;
import com.bank.cuentamovimiento.exception.BusinessException;
import com.bank.cuentamovimiento.exception.ResourceNotFoundException;
import com.bank.cuentamovimiento.repository.CuentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuentaServiceImpl implements CuentaService {

    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public CuentaDTO crear(CuentaDTO dto) {
        if (cuentaRepository.existsByNumeroCuenta(dto.getNumeroCuenta())) {
            throw new BusinessException("Ya existe una cuenta con el número: " + dto.getNumeroCuenta());
        }
        Cuenta cuenta = toEntity(dto);
        cuenta.setSaldoDisponible(dto.getSaldoInicial());
        cuenta = cuentaRepository.save(cuenta);
        return toDTO(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaDTO obtenerPorId(Long id) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con ID: " + id));
        return toDTO(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public CuentaDTO obtenerPorNumeroCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
        return toDTO(cuenta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaDTO> obtenerTodas() {
        return cuentaRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaDTO> obtenerPorClienteId(Long clienteId) {
        return cuentaRepository.findByClienteId(clienteId).stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CuentaDTO actualizar(Long id, CuentaDTO dto) {
        Cuenta cuenta = cuentaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada con ID: " + id));
        cuenta.setTipoCuenta(dto.getTipoCuenta());
        cuenta.setEstado(dto.getEstado());
        if (dto.getClienteId() != null) {
            cuenta.setClienteId(dto.getClienteId());
        }
        if (dto.getClienteNombre() != null) {
            cuenta.setClienteNombre(dto.getClienteNombre());
        }
        cuenta = cuentaRepository.save(cuenta);
        return toDTO(cuenta);
    }

    private CuentaDTO toDTO(Cuenta cuenta) {
        return CuentaDTO.builder()
                .id(cuenta.getId())
                .numeroCuenta(cuenta.getNumeroCuenta())
                .tipoCuenta(cuenta.getTipoCuenta())
                .saldoInicial(cuenta.getSaldoInicial())
                .saldoDisponible(cuenta.getSaldoDisponible())
                .estado(cuenta.getEstado())
                .clienteId(cuenta.getClienteId())
                .clienteNombre(cuenta.getClienteNombre())
                .build();
    }

    private Cuenta toEntity(CuentaDTO dto) {
        return Cuenta.builder()
                .numeroCuenta(dto.getNumeroCuenta())
                .tipoCuenta(dto.getTipoCuenta())
                .saldoInicial(dto.getSaldoInicial())
                .saldoDisponible(dto.getSaldoInicial())
                .estado(dto.getEstado())
                .clienteId(dto.getClienteId())
                .clienteNombre(dto.getClienteNombre())
                .build();
    }
}
