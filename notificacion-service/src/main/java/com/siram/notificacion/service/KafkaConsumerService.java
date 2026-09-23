// KafkaConsumerService.java (Procesador e Idempotencia)
package com.siram.notificacion.service;

import com.siram.notificacion.config.MapeoProperties;
import com.siram.notificacion.dto.IncidenteEvent;
import com.siram.notificacion.model.EventoProcesado;
import com.siram.notificacion.repository.EventoProcesadoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final EventoProcesadoRepository eventoProcesadoRepository;
    private final MapeoProperties mapeoProperties;

    @KafkaListener(topics = "incident-reported", groupId = "${spring.kafka.consumer.group-id}")
    public void consumirIncidente(IncidenteEvent event) {
        var eventId = Objects.requireNonNull(event.getEventId(), "El eventId no puede ser nulo");

        // Control de Idempotencia
        if (eventoProcesadoRepository.existsById(eventId)) {
            log.info("Evento con ID {} ya fue procesado previamente. Ignorando.", eventId);
            return;
        }

        // Determinar entidad y teléfono por configuración interna
        String tipoStr = event.getTipo().name();
        String entidad = mapeoProperties.getEntidades().getOrDefault(tipoStr, "POLICIA");
        String telefono = mapeoProperties.getTelefonos().getOrDefault(entidad, "+570000000000");

        // Simulación de envío de SMS
        log.info("=========================================================================");
        log.info("[SMS SIMULADO ENVIADO A: {} ({})]", entidad, telefono);
        log.info("Mensaje: Reporte de emergencia tipo [{}] en Lat: {}, Lng: {}. Fecha: {}",
                event.getTipo(),
                event.getUbicacion().getLatitud(),
                event.getUbicacion().getLongitud(),
                event.getFechaHora());
        log.info("=========================================================================");

        // Registrar evento procesado
        EventoProcesado procesado = EventoProcesado.builder()
                .eventId(eventId)
                .fechaProcesado(LocalDateTime.now())
                .build();
        eventoProcesadoRepository.save(Objects.requireNonNull(procesado, "El evento procesado no puede ser nulo"));
    }
}