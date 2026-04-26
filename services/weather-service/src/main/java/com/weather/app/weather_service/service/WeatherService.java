package com.weather.app.weather_service.service;

import com.weather.app.weather_service.WeatherProvider;
import com.weather.app.weather_service.domain.RadarData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
public class WeatherService{

    private final WeatherProvider primaryProvider;
    private final RadarCacheService cacheService;

    public WeatherService(WeatherProvider primaryProvider, RadarCacheService cacheService) {
        this.primaryProvider = primaryProvider;
        this.cacheService = cacheService;
    }

    public Optional<RadarData> getRadar(BigDecimal lat, BigDecimal lon){
        log.info("Buscando radar para lat={} lon={}", lat, lon);

        var cached = cacheService.get(lat, lon);
        if (cached.isPresent()) {
            log.info("Cache hit para lat={} lon={}", lat, lon);
            return cached;
        }


        log.info("Cache miss — chamando provider");
        var result=primaryProvider.fetchRadarData(lat, lon);

        result.ifPresent(radar -> {
            log.info("Salvando no Redis source={} riskLevel={}", radar.source(), radar.riskLevel());
            cacheService.set(radar);
        });

        return result;
    }
}
