package com.siram.gateway.controller;

import com.siram.gateway.client.EmergenciasRestClient;
import com.siram.gateway.dto.IncidenteDTO;
import com.siram.gateway.dto.TipoIncidente;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IncidenteGraphQLControllerTest {

    @Test
    void incidentes_pasaTipoYFechaAlClienteRest() {
        EmergenciasRestClient restClient = mock(EmergenciasRestClient.class);
        IncidenteGraphQLController controller = new IncidenteGraphQLController(restClient);
        List<IncidenteDTO> expected = List.of();
        String fecha = "2026-11-19";
        when(restClient.listar("HURTO", fecha)).thenReturn(expected);

        assertSame(expected, controller.incidentes(TipoIncidente.HURTO, fecha));
        verify(restClient).listar("HURTO", fecha);
    }
}
