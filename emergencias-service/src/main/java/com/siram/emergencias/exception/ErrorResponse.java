package com.siram.emergencias.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String mensaje;
    private String detalle;
}
