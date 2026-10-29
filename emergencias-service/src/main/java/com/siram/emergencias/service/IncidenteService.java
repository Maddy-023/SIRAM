// IncidenteService.java (ACTUALIZADO)
// Reemplaza el archivo en: emergencias-service/src/main/java/com/siram/emergencias/service/
package com.siram.emergencias.service;

import com.siram.emergencias.dto.*;
import com.siram.emergencias.exception.IncidenteNoEncontradoException;
import com.siram.emergencias.model.Incidente;
import com.siram.emergencias.model.TipoIncidente;
import com.siram.emergencias.repository.IncidenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncidenteService {

    private final IncidenteRepository incidenteRepository;
    private final KafkaTemplate<String, IncidenteEvent> kafkaTemplate;

    @Transactional
    public IncidenteResponseDTO registrarIncidente(IncidenteInputDTO dto) {
        Objects.requireNonNull(dto, "El payload del incidente no puede ser nulo");
        Objects.requireNonNull(dto.getTipo(), "El tipo del incidente es obligatorio");
        Objects.requireNonNull(dto.getUbicacion(), "La ubicación es obligatoria");
        Objects.requireNonNull(dto.getUbicacion().getLatitud(), "La latitud es obligatoria");
        Objects.requireNonNull(dto.getUbicacion().getLongitud(), "La longitud es obligatoria");

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
        kafkaTemplate.send("incident-reported", key, event)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        log.error("No se pudo publicar el evento de incidente {}", key, error);
                    } else {
                        log.debug("Evento de incidente {} publicado en Kafka", key);
                    }
                });

        return toResponseDTO(guardado);
    }

    public List<IncidenteResponseDTO> listarIncidentes(TipoIncidente tipo, LocalDate fecha) {
        List<Incidente> incidentes;
        if (fecha != null) {
            LocalDateTime inicio = fecha.atStartOfDay();
            LocalDateTime fin = fecha.plusDays(1).atStartOfDay();
            incidentes = tipo != null
                    ? incidenteRepository.findByTipoAndFechaHoraGreaterThanEqualAndFechaHoraLessThan(
                            tipo, inicio, fin)
                    : incidenteRepository.findByFechaHoraGreaterThanEqualAndFechaHoraLessThan(inicio, fin);
        } else {
            incidentes = tipo != null
                    ? incidenteRepository.findByTipo(tipo)
                    : incidenteRepository.findAll();
        }

        return incidentes.stream().map(this::toResponseDTO).toList();
    }

    public IncidenteResponseDTO buscarPorId(UUID id) {
        Incidente incidente = incidenteRepository.findById(id)
                .orElseThrow(() -> new IncidenteNoEncontradoException(id));
        return toResponseDTO(incidente);
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