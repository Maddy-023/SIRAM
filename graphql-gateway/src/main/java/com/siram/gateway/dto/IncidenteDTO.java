package com.siram.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IncidenteDTO {
    private UUID id;
    private TipoIncidente tipo;
    private CoordenadasDTO ubicacion;
    private LocalDateTime fechaHora;
}
