// IncidenteController.java (ACTUALIZADO)
// Reemplaza el archivo en: emergencias-service/src/main/java/com/siram/emergencias/controller/
package com.siram.emergencias.controller;

import com.siram.emergencias.dto.IncidenteInputDTO;
import com.siram.emergencias.dto.IncidenteResponseDTO;
import com.siram.emergencias.model.TipoIncidente;
import com.siram.emergencias.service.IncidenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/incidentes")
@RequiredArgsConstructor
public class IncidenteController {

    private final IncidenteService incidenteService;

    @PostMapping
    public ResponseEntity<IncidenteResponseDTO> registrar(@Valid @RequestBody IncidenteInputDTO input) {
        IncidenteResponseDTO nuevo = incidenteService.registrarIncidente(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    @GetMapping
    public ResponseEntity<List<IncidenteResponseDTO>> listar(
            @RequestParam(required = false) TipoIncidente tipo,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(incidenteService.listarIncidentes(tipo, fecha));
    }
}