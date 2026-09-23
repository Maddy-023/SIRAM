package com.siram.gateway.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class IncidenteRestDTO {
    private UUID id;
    private TipoIncidente tipo;
    private Double ubicacionLat;
    private Double ubicacionLng;
    private LocalDateTime fechaHora;
}
