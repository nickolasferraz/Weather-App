package com.weather.app.weather_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.List;

public record ResponseOpenWeatherDTO(

        BigDecimal lat,
        BigDecimal lon,
        String timezone,

        @JsonProperty("timezone_offset")
        Integer timezoneOffset,

        CurrentDTO current,
        List<MinutelyDTO> minutely,
        List<HourlyDTO> hourly,
        List<DailyDTO> daily,
        List<AlertDTO> alerts

) {
    public record CurrentDTO(
            Long dt,
            Long sunrise,
            Long sunset,
            BigDecimal temp,
            @JsonProperty("feels_like") BigDecimal feelsLike,
            Integer pressure,
            Integer humidity,
            @JsonProperty("dew_point") BigDecimal dewPoint,
            BigDecimal uvi,
            Integer clouds,
            Integer visibility,
            @JsonProperty("wind_speed") BigDecimal windSpeed,
            @JsonProperty("wind_deg") Integer windDeg,
            @JsonProperty("wind_gust") BigDecimal windGust,
            List<WeatherDescriptionDTO> weather
    ) {}

    public record MinutelyDTO(
            Long dt,
            BigDecimal precipitation
    ) {}

    public record HourlyDTO(
            Long dt,
            BigDecimal temp,
            @JsonProperty("feels_like") BigDecimal feelsLike,
            Integer pressure,
            Integer humidity,
            @JsonProperty("dew_point") BigDecimal dewPoint,
            BigDecimal uvi,
            Integer clouds,
            Integer visibility,
            @JsonProperty("wind_speed") BigDecimal windSpeed,
            @JsonProperty("wind_deg") Integer windDeg,
            @JsonProperty("wind_gust") BigDecimal windGust,
            BigDecimal pop,
            List<WeatherDescriptionDTO> weather
    ) {}

    public record DailyDTO(
            Long dt,
            Long sunrise,
            Long sunset,
            Long moonrise,
            Long moonset,
            @JsonProperty("moon_phase") BigDecimal moonPhase,
            String summary,
            TempDTO temp,
            @JsonProperty("feels_like") FeelsLikeDTO feelsLike,
            Integer pressure,
            Integer humidity,
            @JsonProperty("dew_point") BigDecimal dewPoint,
            @JsonProperty("wind_speed") BigDecimal windSpeed,
            @JsonProperty("wind_deg") Integer windDeg,
            @JsonProperty("wind_gust") BigDecimal windGust,
            Integer clouds,
            BigDecimal pop,
            BigDecimal rain,
            BigDecimal uvi,
            List<WeatherDescriptionDTO> weather
    ) {}

    public record TempDTO(
            BigDecimal day,
            BigDecimal min,
            BigDecimal max,
            BigDecimal night,
            BigDecimal eve,
            BigDecimal morn
    ) {}

    public record FeelsLikeDTO(
            BigDecimal day,
            BigDecimal night,
            BigDecimal eve,
            BigDecimal morn
    ) {}

    public record AlertDTO(
            @JsonProperty("sender_name") String senderName,
            String event,
            Long start,
            Long end,
            String description,
            List<String> tags
    ) {}

    public record WeatherDescriptionDTO(
            Integer id,
            String main,
            String description,
            String icon
    ) {}
}

