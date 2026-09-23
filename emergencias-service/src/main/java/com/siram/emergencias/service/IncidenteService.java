// IncidenteService.java
package com.siram.emergencias.service;

import com.siram.emergencias.dto.*;
import com.siram.emergencias.model.Incidente;
import com.siram.emergencias.model.TipoIncidente;
import com.siram.emergencias.repository.IncidenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final KafkaTemplate<String, IncidenteEvent> kafkaTemplate;

    @Transactional
    @SuppressWarnings("null")
    public Incidente registrarIncidente(IncidenteInputDTO dto) {
        Incidente incidente = Incidente.builder()
                .tipo(dto.getTipo())
                .ubicacionLat(dto.getUbicacion().getLatitud())
                .ubicacionLng(dto.getUbicacion().getLongitud())
                .fechaHora(LocalDateTime.now())
                .build();

        Incidente guardado = incidenteRepository.save(Objects.requireNonNull(incidente, "El incidente no puede ser nulo"));

        // Publicar evento en Kafka
       IncidenteEvent event = IncidenteEvent.builder()
                .eventId(guardado.getId())
                .tipo(guardado.getTipo())
                .ubicacion(dto.getUbicacion())
                .fechaHora(guardado.getFechaHora())
                .build();

        String key = Objects.requireNonNull(guardado.getId(), "El id del incidente no puede ser nulo").toString();
        kafkaTemplate.send("incident-reported", key, event);

        return guardado;
    }

    public List<Incidente> listarIncidentes(TipoIncidente tipo) {
        if (tipo != null) {
            return incidenteRepository.findByTipo(tipo);
        }
        return incidenteRepository.findAll();
    }
}