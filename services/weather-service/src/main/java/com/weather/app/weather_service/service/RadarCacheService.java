package com.weather.app.weather_service.service;


import com.weather.app.weather_service.domain.RadarData;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Optional;

@Component
public class RadarCacheService {
    private static final Duration TTL = Duration.ofMinutes(10);
    private static final String PREFIX = "radar";

    private final RedisTemplate<String, RadarData> redisTemplate;

    public RadarCacheService(RedisTemplate<String, RadarData> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Optional<RadarData> get(BigDecimal lat, BigDecimal lon) {
        var value = redisTemplate.opsForValue().get(buildKey(lat, lon));
        return Optional.ofNullable(value);
    }

    public void set(RadarData radarData) {
        redisTemplate.opsForValue().set(
                buildKey(radarData.lat(), radarData.lon()),
                radarData,
                TTL
        );
    }

    public void invalidate(BigDecimal lat, BigDecimal lon) {
        redisTemplate.delete(buildKey(lat, lon));
    }

    private String buildKey(BigDecimal lat, BigDecimal lon) {
        var roundedLat = lat.setScale(2, RoundingMode.HALF_UP);
        var roundedLon = lon.setScale(2, RoundingMode.HALF_UP);
        return "%s:%s:%s".formatted(PREFIX, roundedLat, roundedLon);
    }
}


