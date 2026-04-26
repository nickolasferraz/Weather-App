package com.weather.app.weather_service.dto;

import java.math.BigDecimal;

public record RequestOpenWeatherDTO(

        BigDecimal lat,
        BigDecimal lon


) {
}
