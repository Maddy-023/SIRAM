package com.siram.emergencias.controller;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siram.emergencias.dto.CoordenadasDTO;
import com.siram.emergencias.dto.IncidenteInputDTO;
import com.siram.emergencias.dto.IncidenteResponseDTO;
import com.siram.emergencias.model.TipoIncidente;
import com.siram.emergencias.service.IncidenteService;

@WebMvcTest(IncidenteController.class)
class IncidenteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidenteService incidenteService;

    @Test
    void registrarIncidente_conDatosValidos_retorna201() throws Exception {
        CoordenadasDTO ubicacion = new CoordenadasDTO();
        ubicacion.setLatitud(4.6097);
        ubicacion.setLongitud(-74.0817);

        IncidenteInputDTO input = new IncidenteInputDTO();
        input.setTipo(TipoIncidente.HURTO);
        input.setUbicacion(ubicacion);

        IncidenteResponseDTO response = IncidenteResponseDTO.builder()
                .id(UUID.randomUUID())
                .tipo(TipoIncidente.HURTO)
                .ubicacion(ubicacion)
                .fechaHora(LocalDateTime.now())
                .build();

        given(incidenteService.registrarIncidente(any(IncidenteInputDTO.class))).willReturn(response);

        mockMvc.perform(post("/api/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(input)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("HURTO"));
    }

    @Test
    void registrarIncidente_conDatosInvalidos_retorna400() throws Exception {
        String invalidJson = "{\"tipo\":\"HURTO\",\"ubicacion\":{\"latitud\":4.6}}";

        mockMvc.perform(post("/api/incidentes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.detalle").exists())
                .andExpect(jsonPath("$.message").doesNotExist());
    }

    @Test
    void listarIncidentes_retornaArrayYFiltraPorTipoYFecha() throws Exception {
        LocalDate fecha = LocalDate.of(2026, 10, 3);
        given(incidenteService.listarIncidentes(eq(TipoIncidente.HURTO), eq(fecha)))
                .willReturn(List.of());

        mockMvc.perform(get("/api/incidentes")
                        .param("tipo", "HURTO")
                        .param("fecha", "2026-10-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
