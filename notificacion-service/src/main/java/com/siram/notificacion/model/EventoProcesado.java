// EventoProcesado.java (Control de Idempotencia)
package com.siram.notificacion.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "eventos_procesados")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoProcesado {
    @Id
    private UUID eventId;
    private LocalDateTime fechaProcesado;
}