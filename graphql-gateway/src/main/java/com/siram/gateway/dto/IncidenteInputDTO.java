package com.siram.gateway.dto;

import lombok.Data;

@Data
public class IncidenteInputDTO {
    private TipoIncidente tipo;
    private CoordenadasDTO ubicacion;
}
