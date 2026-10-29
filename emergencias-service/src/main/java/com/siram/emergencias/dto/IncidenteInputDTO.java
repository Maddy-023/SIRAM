// IncidenteInputDTO.java
package com.siram.emergencias.dto;

import com.siram.emergencias.model.TipoIncidente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidenteInputDTO {
    @NotNull(message = "El tipo del incidente es obligatorio")
    private TipoIncidente tipo;

    @NotNull(message = "La ubicación es obligatoria")
    @Valid
    private CoordenadasDTO ubicacion;
}