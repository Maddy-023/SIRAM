// EmergenciasRestClient.java
package com.siram.gateway.client;

import com.siram.gateway.dto.CoordenadasDTO;
import com.siram.gateway.dto.IncidenteDTO;
import com.siram.gateway.dto.IncidenteInputDTO;
import com.siram.gateway.dto.IncidenteRestDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class EmergenciasRestClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${emergencias.service-url:http://localhost:8081}")
    private String emergenciasServiceUrl;

    public IncidenteDTO reportar(IncidenteInputDTO input) {
        String url = emergenciasServiceUrl + "/api/incidentes";
        IncidenteRestDTO response = restTemplate.postForObject(url, input, IncidenteRestDTO.class);
        return map(response);
    }

    public List<IncidenteDTO> listar(String tipo) {
        String url = emergenciasServiceUrl + "/api/incidentes" + (tipo != null ? "?tipo=" + tipo : "");
        IncidenteRestDTO[] response = restTemplate.getForObject(url, IncidenteRestDTO[].class);
        List<IncidenteDTO> incidentes = new ArrayList<>();
        if (response != null) {
            for (IncidenteRestDTO item : response) {
                incidentes.add(map(item));
            }
        }
        return incidentes;
    }

    private IncidenteDTO map(IncidenteRestDTO response) {
        if (response == null) {
            return null;
        }

        IncidenteDTO dto = new IncidenteDTO();
        dto.setId(response.getId());
        dto.setTipo(response.getTipo());
        dto.setFechaHora(response.getFechaHora());

        CoordenadasDTO coordenadas = new CoordenadasDTO();
        coordenadas.setLatitud(response.getUbicacionLat());
        coordenadas.setLongitud(response.getUbicacionLng());
        dto.setUbicacion(coordenadas);

        return dto;
    }
}