package com.siram.gateway.controller;

import com.siram.gateway.client.EmergenciasRestClient;
import com.siram.gateway.dto.IncidenteDTO;
import com.siram.gateway.dto.IncidenteInputDTO;
import com.siram.gateway.dto.TipoIncidente;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class IncidenteGraphQLController {

    private final EmergenciasRestClient emergenciasRestClient;

    public IncidenteGraphQLController(EmergenciasRestClient emergenciasRestClient) {
        this.emergenciasRestClient = emergenciasRestClient;
    }

    @MutationMapping
    public IncidenteDTO reportarIncidente(@Argument("input") IncidenteInputDTO input) {
        return emergenciasRestClient.reportar(input);
    }

    @QueryMapping
    public List<IncidenteDTO> incidentes(
            @Argument("tipo") TipoIncidente tipo,
            @Argument("fecha") String fecha) {
        return emergenciasRestClient.listar(tipo != null ? tipo.name() : null, fecha);
    }
}