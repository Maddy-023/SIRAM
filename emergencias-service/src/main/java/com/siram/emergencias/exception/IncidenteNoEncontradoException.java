package com.siram.emergencias.exception;

import java.util.UUID;

public class IncidenteNoEncontradoException extends RuntimeException {

    public IncidenteNoEncontradoException(UUID id) {
        super("No existe un incidente con el identificador " + id);
    }
}
