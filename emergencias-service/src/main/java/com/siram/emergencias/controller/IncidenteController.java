// IncidenteController.java
package com.siram.emergencias.controller;

import com.siram.emergencias.dto.IncidenteInputDTO;
import com.siram.emergencias.model.Incidente;
import com.siram.emergencias.model.TipoIncidente;
import com.siram.emergencias.service.IncidenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService incidenteService;

    @PostMapping
    public ResponseEntity<Incidente> registrar(@RequestBody IncidenteInputDTO input) {
        Incidente nuevo = incidenteService.registrarIncidente(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @GetMapping
    public ResponseEntity<List<Incidente>> listar(@RequestParam(required = false) TipoIncidente tipo) {
        return ResponseEntity.ok(incidenteService.listarIncidentes(tipo));
    }
}