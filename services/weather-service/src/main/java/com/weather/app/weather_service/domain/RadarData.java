package com.weather.app.weather_service.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RadarData(

        BigDecimal lat,
        BigDecimal lon,
        BigDecimal precipitation,
        String riskLevel,
        LocalDateTime collectedAt,
        String source

) {}