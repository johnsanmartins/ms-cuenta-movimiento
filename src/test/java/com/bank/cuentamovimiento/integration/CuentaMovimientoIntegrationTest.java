package com.bank.cuentamovimiento.integration;

import com.bank.cuentamovimiento.dto.CuentaDTO;
import com.bank.cuentamovimiento.dto.MovimientoDTO;
import com.bank.cuentamovimiento.entity.Cuenta;
import com.bank.cuentamovimiento.repository.CuentaRepository;
import com.bank.cuentamovimiento.repository.MovimientoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockbean.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Pruebas de integración - Cuenta y Movimiento")
class CuentaMovimientoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CuentaRepository cuentaRepository;

    @Autowired
    private MovimientoRepository movimientoRepository;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private ConnectionFactory connectionFactory;

    @BeforeEach
    void setUp() {
        movimientoRepository.deleteAll();
        cuentaRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("Debe crear una cuenta exitosamente via API")
    void crearCuenta_Integration() throws Exception {
        CuentaDTO cuentaDTO = CuentaDTO.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Ahorros")
                .saldoInicial(new BigDecimal("2000"))
                .estado(true)
                .clienteId(1L)
                .clienteNombre("Jose Lema")
                .build();

        mockMvc.perform(post("/cuentas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cuentaDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numeroCuenta").value("478758"))
                .andExpect(jsonPath("$.tipoCuenta").value("Ahorros"))
                .andExpect(jsonPath("$.saldoDisponible").value(2000));

        assertThat(cuentaRepository.findByNumeroCuenta("478758")).isPresent();
    }

    @Test
    @Order(2)
    @DisplayName("Debe registrar un depósito y actualizar saldo")
    void registrarDeposito_Integration() throws Exception {
        // Crear cuenta primero
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta("225487")
                .tipoCuenta("Corriente")
                .saldoInicial(new BigDecimal("100"))
                .saldoDisponible(new BigDecimal("100"))
                .estado(true)
                .clienteId(2L)
                .clienteNombre("Marianela Montalvo")
                .build();
        cuentaRepository.save(cuenta);

        // Registrar depósito
        MovimientoDTO movDTO = MovimientoDTO.builder()
                .tipoMovimiento("Deposito")
                .valor(new BigDecimal("600"))
                .numeroCuenta("225487")
                .build();

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipoMovimiento").value("Deposito"))
                .andExpect(jsonPath("$.saldo").value(700));

        Cuenta cuentaActualizada = cuentaRepository.findByNumeroCuenta("225487").orElseThrow();
        assertThat(cuentaActualizada.getSaldoDisponible()).isEqualByComparingTo(new BigDecimal("700"));
    }

    @Test
    @Order(3)
    @DisplayName("Debe registrar un retiro y actualizar saldo")
    void registrarRetiro_Integration() throws Exception {
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta("478758")
                .tipoCuenta("Ahorros")
                .saldoInicial(new BigDecimal("2000"))
                .saldoDisponible(new BigDecimal("2000"))
                .estado(true)
                .clienteId(1L)
                .clienteNombre("Jose Lema")
                .build();
        cuentaRepository.save(cuenta);

        MovimientoDTO movDTO = MovimientoDTO.builder()
                .tipoMovimiento("Retiro")
                .valor(new BigDecimal("575"))
                .numeroCuenta("478758")
                .build();

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldo").value(1425));

        Cuenta cuentaActualizada = cuentaRepository.findByNumeroCuenta("478758").orElseThrow();
        assertThat(cuentaActualizada.getSaldoDisponible()).isEqualByComparingTo(new BigDecimal("1425"));
    }

    @Test
    @Order(4)
    @DisplayName("F3 - Debe rechazar retiro cuando saldo es insuficiente")
    void retiroSaldoInsuficiente_Integration() throws Exception {
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta("495878")
                .tipoCuenta("Ahorros")
                .saldoInicial(BigDecimal.ZERO)
                .saldoDisponible(BigDecimal.ZERO)
                .estado(true)
                .clienteId(3L)
                .clienteNombre("Juan Osorio")
                .build();
        cuentaRepository.save(cuenta);

        MovimientoDTO movDTO = MovimientoDTO.builder()
                .tipoMovimiento("Retiro")
                .valor(new BigDecimal("100"))
                .numeroCuenta("495878")
                .build();

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Saldo no disponible"));
    }

    @Test
    @Order(5)
    @DisplayName("F4 - Debe generar reporte de estado de cuenta")
    void generarReporte_Integration() throws Exception {
        // Crear cuenta con movimientos
        Cuenta cuenta = Cuenta.builder()
                .numeroCuenta("225487")
                .tipoCuenta("Corriente")
                .saldoInicial(new BigDecimal("100"))
                .saldoDisponible(new BigDecimal("100"))
                .estado(true)
                .clienteId(2L)
                .clienteNombre("Marianela Montalvo")
                .build();
        cuentaRepository.save(cuenta);

        // Registrar un movimiento
        MovimientoDTO movDTO = MovimientoDTO.builder()
                .tipoMovimiento("Deposito")
                .valor(new BigDecimal("600"))
                .numeroCuenta("225487")
                .build();

        mockMvc.perform(post("/movimientos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(movDTO)))
                .andExpect(status().isCreated());

        // Consultar reporte
        mockMvc.perform(get("/reportes")
                        .param("cliente", "Marianela Montalvo")
                        .param("fechaInicio", "2020-01-01")
                        .param("fechaFin", "2030-12-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cliente").value("Marianela Montalvo"))
                .andExpect(jsonPath("$.cuentas").isArray())
                .andExpect(jsonPath("$.cuentas[0].numeroCuenta").value("225487"));
    }

    @Test
    @Order(6)
    @DisplayName("Debe listar todas las cuentas")
    void listarCuentas_Integration() throws Exception {
        Cuenta cuenta1 = Cuenta.builder()
                .numeroCuenta("111111").tipoCuenta("Ahorros")
                .saldoInicial(new BigDecimal("500")).saldoDisponible(new BigDecimal("500"))
                .estado(true).clienteId(1L).clienteNombre("Test 1").build();
        Cuenta cuenta2 = Cuenta.builder()
                .numeroCuenta("222222").tipoCuenta("Corriente")
                .saldoInicial(new BigDecimal("1000")).saldoDisponible(new BigDecimal("1000"))
                .estado(true).clienteId(2L).clienteNombre("Test 2").build();
        cuentaRepository.save(cuenta1);
        cuentaRepository.save(cuenta2);

        mockMvc.perform(get("/cuentas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}
