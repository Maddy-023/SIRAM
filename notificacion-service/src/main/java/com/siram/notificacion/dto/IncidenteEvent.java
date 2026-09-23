package com.siram.notificacion.dto;

import com.siram.notificacion.model.TipoIncidente;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenteEvent {
    private UUID eventId;
    private TipoIncidente tipo;
    private CoordenadasDTO ubicacion;
    private LocalDateTime fechaHora;
}
