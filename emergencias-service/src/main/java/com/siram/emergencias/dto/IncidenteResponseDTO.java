// IncidenteResponseDTO.java
// Va en: emergencias-service/src/main/java/com/siram/emergencias/dto/
package com.siram.emergencias.dto;

import com.siram.emergencias.model.TipoIncidente;
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
public class IncidenteResponseDTO {
    private UUID id;
    private TipoIncidente tipo;
    private CoordenadasDTO ubicacion;
    private LocalDateTime fechaHora;
}