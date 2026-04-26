package com.weather.app.weather_service;

import com.weather.app.weather_service.domain.RadarData;
import com.weather.app.weather_service.dto.ResponseOpenWeatherDTO;
import com.weather.app.weather_service.providers.OpenWeatherMapProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenWeatherMapProviderTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenWeatherMapProvider provider;

    private final BigDecimal LAT = new BigDecimal("-23.5505");
    private final BigDecimal LON = new BigDecimal("-46.6333");
    private final long EPOCH_NOW = 1_700_000_000L;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(provider, "apikey", "fake-api-key");
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private ResponseOpenWeatherDTO.CurrentDTO buildCurrentDTO() {
        return new ResponseOpenWeatherDTO.CurrentDTO(
                EPOCH_NOW,          // dt
                1_700_000_100L,     // sunrise
                1_700_040_000L,     // sunset
                new BigDecimal("22.5"),  // temp
                new BigDecimal("21.0"),  // feelsLike
                1013,               // pressure
                80,                 // humidity
                new BigDecimal("18.0"),  // dewPoint
                new BigDecimal("3.5"),   // uvi
                20,                 // clouds
                10000,              // visibility
                new BigDecimal("5.0"),   // windSpeed
                180,                // windDeg
                new BigDecimal("7.0"),   // windGust
                List.of()           // weather
        );
    }

    private ResponseOpenWeatherDTO buildResponse(List<BigDecimal> precipitations) {
        List<ResponseOpenWeatherDTO.MinutelyDTO> minutely = precipitations.stream()
                .map(p -> new ResponseOpenWeatherDTO.MinutelyDTO(EPOCH_NOW, p))
                .toList();

        return new ResponseOpenWeatherDTO(
                LAT,            // lat
                LON,            // lon
                "America/Sao_Paulo", // timezone
                -10800,         // timezoneOffset
                buildCurrentDTO(),   // current
                minutely,       // minutely
                List.of(),      // hourly
                List.of(),      // daily
                List.of()       // alerts
        );
    }

    private void mockRestTemplate(ResponseOpenWeatherDTO dto) {
        when(restTemplate.getForEntity(any(URI.class), eq(ResponseOpenWeatherDTO.class)))
                .thenReturn(ResponseEntity.ok(dto));
    }

    // ── testes ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Deve retornar LOW quando não há precipitação")
    void shouldReturnLowRiskWhenNoPrecipitation() {
        mockRestTemplate(buildResponse(List.of(BigDecimal.ZERO, BigDecimal.ZERO)));

        Optional<RadarData> result = provider.fetchRadarData(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().riskLevel()).isEqualTo("LOW");
        assertThat(result.get().precipitation()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Deve retornar MEDIUM quando precipitação <= 2.5")
    void shouldReturnMediumRisk() {
        mockRestTemplate(buildResponse(List.of(new BigDecimal("1.0"), new BigDecimal("2.5"))));

        Optional<RadarData> result = provider.fetchRadarData(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().riskLevel()).isEqualTo("MEDIUM");
        assertThat(result.get().precipitation()).isEqualByComparingTo("2.5");
    }

    @Test
    @DisplayName("Deve retornar HIGH quando precipitação > 2.5")
    void shouldReturnHighRisk() {
        mockRestTemplate(buildResponse(List.of(new BigDecimal("5.0"), new BigDecimal("10.0"))));

        Optional<RadarData> result = provider.fetchRadarData(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().riskLevel()).isEqualTo("HIGH");
        assertThat(result.get().precipitation()).isEqualByComparingTo("10.0");
    }

    @Test
    @DisplayName("Deve ignorar minutos além do índice 30")
    void shouldConsiderOnly30MinutesWindow() {
        List<BigDecimal> precipitations = new ArrayList<>();
        for (int i = 0; i < 30; i++) precipitations.add(new BigDecimal("1.0")); // janela válida
        for (int i = 0; i < 30; i++) precipitations.add(new BigDecimal("99.0")); // deve ser ignorado

        mockRestTemplate(buildResponse(precipitations));

        Optional<RadarData> result = provider.fetchRadarData(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().precipitation()).isEqualByComparingTo("1.0");
        assertThat(result.get().riskLevel()).isEqualTo("MEDIUM");
    }

    @Test
    @DisplayName("Deve retornar LOW quando minutely está vazio")
    void shouldReturnLowWhenMinutelyIsEmpty() {
        mockRestTemplate(buildResponse(List.of()));

        Optional<RadarData> result = provider.fetchRadarData(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().riskLevel()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("Deve retornar LOW quando minutely é null")
    void shouldReturnLowWhenMinutelyIsNull() {
        var dto = new ResponseOpenWeatherDTO(
                LAT, LON, "America/Sao_Paulo", -10800,
                buildCurrentDTO(),
                null,       // minutely null
                List.of(), List.of(), List.of()
        );
        mockRestTemplate(dto);

        Optional<RadarData> result = provider.fetchRadarData(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().riskLevel()).isEqualTo("LOW");
    }

    @Test
    @DisplayName("Deve preencher lat, lon, source e collectedAt corretamente")
    void shouldFillRadarDataFields() {
        mockRestTemplate(buildResponse(List.of(new BigDecimal("1.0"))));

        RadarData result = provider.fetchRadarData(LAT, LON).orElseThrow();

        assertThat(result.lat()).isEqualByComparingTo(LAT);
        assertThat(result.lon()).isEqualByComparingTo(LON);
        assertThat(result.source()).isEqualTo("OPEN_WEATHER_MAP");
        assertThat(result.collectedAt()).isNotNull();
    }
}