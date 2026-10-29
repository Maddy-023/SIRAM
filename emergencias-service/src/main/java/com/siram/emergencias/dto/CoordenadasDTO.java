// CoordenadasDTO.java
package com.siram.emergencias.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CoordenadasDTO {
    @NotNull(message = "La latitud es obligatoria")
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    private Double longitud;
}