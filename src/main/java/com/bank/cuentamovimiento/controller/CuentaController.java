package com.bank.cuentamovimiento.controller;

import com.bank.cuentamovimiento.dto.CuentaDTO;
import com.bank.cuentamovimiento.service.CuentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cuentas")
@RequiredArgsConstructor
public class CuentaController {

    private final CuentaService cuentaService;

    @PostMapping
    public ResponseEntity<CuentaDTO> crear(@Valid @RequestBody CuentaDTO cuentaDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cuentaService.crear(cuentaDTO));
    }

    @GetMapping
    public ResponseEntity<List<CuentaDTO>> obtenerTodas() {
        return ResponseEntity.ok(cuentaService.obtenerTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaDTO> actualizar(@PathVariable Long id, @Valid @RequestBody CuentaDTO cuentaDTO) {
        return ResponseEntity.ok(cuentaService.actualizar(id, cuentaDTO));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CuentaDTO> actualizarParcial(@PathVariable Long id, @RequestBody CuentaDTO cuentaDTO) {
        return ResponseEntity.ok(cuentaService.actualizar(id, cuentaDTO));
    }
}
