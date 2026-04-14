package com.bank.cuentamovimiento.controller;

import com.bank.cuentamovimiento.dto.MovimientoDTO;
import com.bank.cuentamovimiento.service.MovimientoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/movimientos")
@RequiredArgsConstructor
public class MovimientoController {

    private final MovimientoService movimientoService;

    @PostMapping
    public ResponseEntity<MovimientoDTO> registrar(@Valid @RequestBody MovimientoDTO movimientoDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movimientoService.registrar(movimientoDTO));
    }

    @GetMapping
    public ResponseEntity<List<MovimientoDTO>> obtenerTodos() {
        return ResponseEntity.ok(movimientoService.obtenerTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovimientoDTO> actualizar(@PathVariable Long id, @Valid @RequestBody MovimientoDTO dto) {
        return ResponseEntity.ok(movimientoService.actualizar(id, dto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<MovimientoDTO> actualizarParcial(@PathVariable Long id, @RequestBody MovimientoDTO dto) {
        return ResponseEntity.ok(movimientoService.actualizar(id, dto));
    }
}
