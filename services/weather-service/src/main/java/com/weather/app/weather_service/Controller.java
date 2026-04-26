package com.weather.app.weather_service;



import com.weather.app.weather_service.domain.RadarData;
import com.weather.app.weather_service.dto.RequestOpenWeatherDTO;
import com.weather.app.weather_service.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;

@Slf4j
@RestController
@RequestMapping("/weather")
public class Controller {



    private final WeatherService weatherService;

    public Controller(WeatherService weatherService) {
        this.weatherService = weatherService;
    }


    @PostMapping("/radar")
    public ResponseEntity<RadarData> getWeather(@RequestBody RequestOpenWeatherDTO request) {

        try {
            log.info("controller");
            return weatherService.getRadar(request.lat(), request.lon())
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.noContent().build());

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }



}
