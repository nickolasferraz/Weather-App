package com.weather.app.weather_service.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RedisConnectionTest implements CommandLineRunner {

    private final RedisConnectionFactory redisConnectionFactory;

    public RedisConnectionTest(RedisConnectionFactory redisConnectionFactory) {
        this.redisConnectionFactory = redisConnectionFactory;
    }

    @Override
    public void run(String... args) {
        try {
            String pong = redisConnectionFactory.getConnection().ping();
            log.info("✅ Redis conectado com sucesso! Resposta: {}", pong);
        } catch (Exception e) {
            log.error("❌ Falha ao conectar no Redis: {}", e.getMessage());
        }
    }
}