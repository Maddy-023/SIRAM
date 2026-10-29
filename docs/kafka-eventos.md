# Eventos Kafka de SIRAM

## Tópico `incident-reported`

El microservicio `emergencias-service` publica un evento después de persistir un
incidente. `notificacion-service` consume el evento, determina la entidad
responsable según su configuración interna y registra el `eventId` procesado
para evitar procesarlo más de una vez.

La clave Kafka es el UUID del incidente. El valor es JSON con esta estructura:

```json
{
  "eventId": "21d40a8e-483c-4d4d-9da0-670d3a322636",
  "tipo": "HURTO",
  "ubicacion": {
    "latitud": 0.1,
    "longitud": 0.1
  },
  "fechaHora": "2026-10-03T20:25:09.001531"
}
```

| Campo | Tipo | Descripción |
|---|---|---|
| `eventId` | UUID | Identificador único del incidente y clave de idempotencia. |
| `tipo` | `TipoIncidente` | Categoría definida por el microservicio de Emergencias. |
| `ubicacion.latitud` | número | Latitud del incidente. |
| `ubicacion.longitud` | número | Longitud del incidente. |
| `fechaHora` | fecha/hora ISO-8601 | Momento en que se registró el incidente. |

## Procesamiento, idempotencia y errores

- El consumidor usa el UUID como clave primaria en `eventos_procesados`. Si ya
  existe, registra que el evento es duplicado y lo omite.
- El envío de SMS es simulado y queda registrado en los logs de
  `notificacion-service`.
- El productor registra un error si Kafka no confirma la publicación; una
  publicación confirmada queda en el log de depuración.
- Los logs de contenedores tienen rotación de hasta tres archivos de 10 MB cada
  uno.
- Para desarrollo local, Kafka conserva los segmentos hasta 24 horas y hasta
  512 MB por partición. La retención por tamaño puede eliminar segmentos antes
  de 24 horas; un consumidor desconectado durante ese tiempo podría no recibir
  todos los eventos.
