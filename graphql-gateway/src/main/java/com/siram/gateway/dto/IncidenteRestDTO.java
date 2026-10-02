// IncidenteRestDTO.java (ACTUALIZADO)
// Reemplaza el archivo en: graphql-gateway/src/main/java/com/siram/gateway/dto/
package com.siram.gateway.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class IncidenteRestDTO {
    private UUID id;
    private TipoIncidente tipo;
    private CoordenadasDTO ubicacion;
    private LocalDateTime fechaHora;
}