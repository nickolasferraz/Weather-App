package com.weather.app.weather_service;

import com.weather.app.weather_service.domain.RadarData;
import com.weather.app.weather_service.service.RadarCacheService;
import com.weather.app.weather_service.service.WeatherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WeatherServiceTest {

    @Mock
    private WeatherProvider primaryProvider;

    @Mock
    private RadarCacheService cacheService;

    @InjectMocks
    private WeatherService weatherService;

    private final BigDecimal LAT = new BigDecimal("-23.5505");
    private final BigDecimal LON = new BigDecimal("-46.6333");

    private RadarData buildRadarData(String riskLevel) {
        return new RadarData(
                LAT, LON,
                new BigDecimal("5.0"),
                riskLevel,
                LocalDateTime.now(),
                "OPEN_WEATHER_MAP"
        );
    }

    @Test
    @DisplayName("Deve retornar do cache sem chamar o provider (cache hit)")
    void shouldReturnFromCacheWithoutCallingProvider() {
        var cached = buildRadarData("HIGH");
        when(cacheService.get(LAT, LON)).thenReturn(Optional.of(cached));

        var result = weatherService.getRadar(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().riskLevel()).isEqualTo("HIGH");

        // provider nunca deve ser chamado no cache hit
        verifyNoInteractions(primaryProvider);
    }

    @Test
    @DisplayName("Deve chamar o provider e salvar no cache quando cache miss")
    void shouldCallProviderAndSaveToCacheOnCacheMiss() {
        var radarData = buildRadarData("MEDIUM");
        when(cacheService.get(LAT, LON)).thenReturn(Optional.empty());
        when(primaryProvider.fetchRadarData(LAT, LON)).thenReturn(Optional.of(radarData));

        var result = weatherService.getRadar(LAT, LON);

        assertThat(result).isPresent();
        assertThat(result.get().riskLevel()).isEqualTo("MEDIUM");

        // deve salvar no cache após buscar no provider
        verify(cacheService, times(1)).set(radarData);
    }

    @Test
    @DisplayName("Deve retornar empty quando provider não encontra dados")
    void shouldReturnEmptyWhenProviderReturnsEmpty() {
        when(cacheService.get(LAT, LON)).thenReturn(Optional.empty());
        when(primaryProvider.fetchRadarData(LAT, LON)).thenReturn(Optional.empty());

        var result = weatherService.getRadar(LAT, LON);

        assertThat(result).isEmpty();

        // não deve tentar salvar no cache se não há dados
        verify(cacheService, never()).set(any());
    }
}