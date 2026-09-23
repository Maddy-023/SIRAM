// IncidenteInputDTO.java
package com.siram.emergencias.dto;

import com.siram.emergencias.model.TipoIncidente;
import lombok.Data;

@Data
public class IncidenteInputDTO {
    private TipoIncidente tipo;
    private CoordenadasDTO ubicacion;
}