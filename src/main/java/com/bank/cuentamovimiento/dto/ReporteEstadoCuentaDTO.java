package com.bank.cuentamovimiento.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReporteEstadoCuentaDTO {

    private String cliente;
    private List<CuentaReporteDTO> cuentas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CuentaReporteDTO {
        private String numeroCuenta;
        private String tipo;
        private BigDecimal saldoInicial;
        private Boolean estado;
        private BigDecimal saldoDisponible;
        private List<MovimientoReporteDTO> movimientos;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MovimientoReporteDTO {
        private LocalDateTime fecha;
        private String tipoMovimiento;
        private BigDecimal valor;
        private BigDecimal saldo;
    }
}
