package com.bank.cuentamovimiento.service;

import com.bank.cuentamovimiento.dto.MovimientoDTO;
import com.bank.cuentamovimiento.dto.ReporteEstadoCuentaDTO;
import com.bank.cuentamovimiento.entity.Cuenta;
import com.bank.cuentamovimiento.entity.Movimiento;
import com.bank.cuentamovimiento.exception.ResourceNotFoundException;
import com.bank.cuentamovimiento.exception.SaldoInsuficienteException;
import com.bank.cuentamovimiento.repository.CuentaRepository;
import com.bank.cuentamovimiento.repository.MovimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovimientoServiceImpl implements MovimientoService {

    private final MovimientoRepository movimientoRepository;
    private final CuentaRepository cuentaRepository;

    @Override
    @Transactional
    public MovimientoDTO registrar(MovimientoDTO dto) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(dto.getNumeroCuenta())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + dto.getNumeroCuenta()));

        BigDecimal valorMovimiento = dto.getValor();

        // Determinar si es depósito o retiro basado en el tipo o el signo del valor
        if ("Retiro".equalsIgnoreCase(dto.getTipoMovimiento()) && valorMovimiento.compareTo(BigDecimal.ZERO) > 0) {
            valorMovimiento = valorMovimiento.negate();
        }

        // F3: Validar saldo disponible para retiros
        BigDecimal nuevoSaldo = cuenta.getSaldoDisponible().add(valorMovimiento);
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) < 0) {
            throw new SaldoInsuficienteException();
        }

        // F2: Actualizar saldo disponible
        cuenta.setSaldoDisponible(nuevoSaldo);
        cuentaRepository.save(cuenta);

        // Registrar movimiento
        Movimiento movimiento = Movimiento.builder()
                .fecha(LocalDateTime.now())
                .tipoMovimiento(dto.getTipoMovimiento())
                .valor(valorMovimiento)
                .saldo(nuevoSaldo)
                .cuenta(cuenta)
                .build();

        movimiento = movimientoRepository.save(movimiento);
        return toDTO(movimiento);
    }

    @Override
    @Transactional(readOnly = true)
    public MovimientoDTO obtenerPorId(Long id) {
        Movimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con ID: " + id));
        return toDTO(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerTodos() {
        return movimientoRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MovimientoDTO> obtenerPorCuenta(String numeroCuenta) {
        Cuenta cuenta = cuentaRepository.findByNumeroCuenta(numeroCuenta)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada: " + numeroCuenta));
        return movimientoRepository.findByCuentaIdOrderByFechaDesc(cuenta.getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MovimientoDTO actualizar(Long id, MovimientoDTO dto) {
        Movimiento mov = movimientoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Movimiento no encontrado con ID: " + id));
        mov.setTipoMovimiento(dto.getTipoMovimiento());
        mov.setValor(dto.getValor());
        mov = movimientoRepository.save(mov);
        return toDTO(mov);
    }

    @Override
    @Transactional(readOnly = true)
    public ReporteEstadoCuentaDTO generarReporte(Long clienteId, String clienteNombre,
                                                  LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(LocalTime.MAX);

        // Obtener cuentas del cliente
        List<Cuenta> cuentas;
        String nombreCliente;

        if (clienteId != null) {
            cuentas = cuentaRepository.findByClienteId(clienteId);
            nombreCliente = cuentas.isEmpty() ? "Desconocido" : cuentas.get(0).getClienteNombre();
        } else {
            cuentas = cuentaRepository.findByClienteNombre(clienteNombre);
            nombreCliente = clienteNombre;
        }

        if (cuentas.isEmpty()) {
            throw new ResourceNotFoundException("No se encontraron cuentas para el cliente especificado");
        }

        // Obtener movimientos filtrados por fecha
        List<Movimiento> todosMovimientos;
        if (clienteId != null) {
            todosMovimientos = movimientoRepository.findByClienteIdAndFechaBetween(clienteId, inicio, fin);
        } else {
            todosMovimientos = movimientoRepository.findByClienteNombreAndFechaBetween(clienteNombre, inicio, fin);
        }

        // Agrupar movimientos por cuenta
        Map<Long, List<Movimiento>> movimientosPorCuenta = todosMovimientos.stream()
                .collect(Collectors.groupingBy(m -> m.getCuenta().getId()));

        // Construir reporte
        List<ReporteEstadoCuentaDTO.CuentaReporteDTO> cuentasReporte = cuentas.stream()
                .map(cuenta -> {
                    List<Movimiento> movsCuenta = movimientosPorCuenta.getOrDefault(cuenta.getId(), List.of());

                    List<ReporteEstadoCuentaDTO.MovimientoReporteDTO> movsDTO = movsCuenta.stream()
                            .map(m -> ReporteEstadoCuentaDTO.MovimientoReporteDTO.builder()
                                    .fecha(m.getFecha())
                                    .tipoMovimiento(m.getTipoMovimiento())
                                    .valor(m.getValor())
                                    .saldo(m.getSaldo())
                                    .build())
                            .collect(Collectors.toList());

                    return ReporteEstadoCuentaDTO.CuentaReporteDTO.builder()
                            .numeroCuenta(cuenta.getNumeroCuenta())
                            .tipo(cuenta.getTipoCuenta())
                            .saldoInicial(cuenta.getSaldoInicial())
                            .estado(cuenta.getEstado())
                            .saldoDisponible(cuenta.getSaldoDisponible())
                            .movimientos(movsDTO)
                            .build();
                })
                .collect(Collectors.toList());

        return ReporteEstadoCuentaDTO.builder()
                .cliente(nombreCliente)
                .cuentas(cuentasReporte)
                .build();
    }

    private MovimientoDTO toDTO(Movimiento mov) {
        return MovimientoDTO.builder()
                .id(mov.getId())
                .fecha(mov.getFecha())
                .tipoMovimiento(mov.getTipoMovimiento())
                .valor(mov.getValor())
                .saldo(mov.getSaldo())
                .numeroCuenta(mov.getCuenta().getNumeroCuenta())
                .build();
    }
}
