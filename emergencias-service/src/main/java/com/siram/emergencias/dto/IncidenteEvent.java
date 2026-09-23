// IncidenteEvent.java (Estructura para Kafka)
package com.siram.emergencias.dto;

import com.siram.emergencias.model.TipoIncidente;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IncidenteEvent {
    private UUID eventId;
    private TipoIncidente tipo;
    private CoordenadasDTO ubicacion;
    private LocalDateTime fechaHora;
}