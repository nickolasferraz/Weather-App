package com.weather.app.weather_service;

import com.weather.app.weather_service.domain.RadarData;

import java.math.BigDecimal;
import java.util.Optional;

public interface WeatherProvider {

    Optional<RadarData> fetchRadarData(BigDecimal lat , BigDecimal lon);
}
