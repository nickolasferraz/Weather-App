package com.weather.app.weather_service.providers;

import com.weather.app.weather_service.dto.ResponseOpenWeatherDTO;
import com.weather.app.weather_service.WeatherProvider;
import com.weather.app.weather_service.domain.RadarData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Service
public class OpenWeatherMapProvider  implements WeatherProvider {

//    ele vai até a api e depois Converte para o Openweather
    @Value("${weather.api-key}")
    private  String apikey;

    private final RestTemplate restTemplate;

    public OpenWeatherMapProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @Override
    public Optional<RadarData> fetchRadarData(BigDecimal lat, BigDecimal lon) {

        log.info("Openprovider");

        URI uri = UriComponentsBuilder
                .fromHttpUrl("https://api.openweathermap.org/data/3.0/onecall")
                .queryParam("lat", lat)
                .queryParam("lon", lon)
                .queryParam("appid", apikey)
                .queryParam("units", "metric")
                .build()
                .toUri();


        var jsonbody =restTemplate.getForEntity(uri, ResponseOpenWeatherDTO.class);

        System.out.println("STATUS: " + jsonbody.getStatusCode());
        System.out.println("BODY: " + jsonbody.getBody());

        var precipitation = Optional.ofNullable(jsonbody.getBody().minutely())
                .filter(list-> !list.isEmpty())
                .map(list->list.stream()
                        .limit(30)// Ou seja, os próximos 30 minutos. A API retorna 60 minutos, mas o ChuvaApp só precisa da janela de 30 minutos pela proposta de valor do projeto
                        .map(ResponseOpenWeatherDTO.MinutelyDTO::precipitation)//Extrai só o campo precipitation de cada MinutelyDTO. O stream deixa de ser Stream<MinutelyDTO> e vira Stream<BigDecimal>.
                        .max(BigDecimal::compareTo) // Encontra o maior valor de precipitação nesses 30 minutos. Retorna Optional<BigDecimal> porque o stream poderia estar vazio.
                                                   // Por que o pico e não a média? Porque se vai chover forte em algum minuto dos próximos 30, o usuário precisa saber — a média poderia esconder um pico perigoso
                        .orElse(BigDecimal.ZERO))//Se o stream interno ficar vazio de alguma forma, retorna 0 como precipitação.
                .orElse(BigDecimal.ZERO);

        var collectedAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(jsonbody.getBody().current().dt()),
                ZoneOffset.UTC
        );

        var radarData = new RadarData(
                lat,
                lon,
                precipitation,
                resolveRiskLevel(precipitation),
                collectedAt,
                "OPEN_WEATHER_MAP"
        );


        return Optional.of(radarData);



    }

    private String resolveRiskLevel(BigDecimal precipitation) {
        if (precipitation.compareTo(BigDecimal.ZERO) == 0) return "LOW";
        if (precipitation.compareTo(new BigDecimal("2.5")) <= 0)  return "MEDIUM";
        return "HIGH";
    }
}
