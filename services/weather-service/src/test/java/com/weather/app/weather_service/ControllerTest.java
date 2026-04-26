package com.weather.app.weather_service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather.app.weather_service.domain.RadarData;
import com.weather.app.weather_service.dto.RequestOpenWeatherDTO;
import com.weather.app.weather_service.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(Controller.class)
class ControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WeatherService weatherService;

    private final BigDecimal LAT = new BigDecimal("-23.5505");
    private final BigDecimal LON = new BigDecimal("-46.6333");

    private RadarData buildRadarData() {
        return new RadarData(
                LAT, LON,
                new BigDecimal("5.0"),
                "HIGH",
                LocalDateTime.now(),
                "OPEN_WEATHER_MAP"
        );
    }

    @Test
    @DisplayName("POST /weather/radar deve retornar 200 com RadarData")
    void shouldReturn200WithRadarData() throws Exception {
        var request = new RequestOpenWeatherDTO(LAT, LON);
        when(weatherService.getRadar(LAT, LON)).thenReturn(Optional.of(buildRadarData()));

        mockMvc.perform(post("/weather/radar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.riskLevel").value("HIGH"))
                .andExpect(jsonPath("$.source").value("OPEN_WEATHER_MAP"));
    }

    @Test
    @DisplayName("POST /weather/radar deve retornar 204 quando service retorna empty")
    void shouldReturn204WhenNoContent() throws Exception {
        var request = new RequestOpenWeatherDTO(LAT, LON);
        when(weatherService.getRadar(LAT, LON)).thenReturn(Optional.empty());

        mockMvc.perform(post("/weather/radar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /weather/radar deve retornar 500 quando service lança exceção")
    void shouldReturn500OnUnexpectedException() throws Exception {
        var request = new RequestOpenWeatherDTO(LAT, LON);
        when(weatherService.getRadar(LAT, LON)).thenThrow(new RuntimeException("erro inesperado"));

        mockMvc.perform(post("/weather/radar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /weather/radar deve retornar 400 quando body está ausente")
    void shouldReturn400WhenBodyIsMissing() throws Exception {
        mockMvc.perform(post("/weather/radar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
