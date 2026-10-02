// IncidenteService.java (ACTUALIZADO)
// Reemplaza el archivo en: emergencias-service/src/main/java/com/siram/emergencias/service/
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final KafkaTemplate<String, IncidenteEvent> kafkaTemplate;

    @Transactional
    @SuppressWarnings("null")
    public IncidenteResponseDTO registrarIncidente(IncidenteInputDTO dto) {
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

        return toResponseDTO(guardado);
    }

    public List<IncidenteResponseDTO> listarIncidentes(TipoIncidente tipo) {
        List<Incidente> incidentes = (tipo != null)
                ? incidenteRepository.findByTipo(tipo)
                : incidenteRepository.findAll();

        return incidentes.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    // Traduce la entidad (campos planos ubicacionLat/ubicacionLng) al formato
    // que promete el contrato OpenAPI (ubicacion: {latitud, longitud} anidado).
    private IncidenteResponseDTO toResponseDTO(Incidente incidente) {
        CoordenadasDTO ubicacion = new CoordenadasDTO();
        ubicacion.setLatitud(incidente.getUbicacionLat());
        ubicacion.setLongitud(incidente.getUbicacionLng());

        return IncidenteResponseDTO.builder()
                .id(incidente.getId())
                .tipo(incidente.getTipo())
                .ubicacion(ubicacion)
                .fechaHora(incidente.getFechaHora())
                .build();
    }
}