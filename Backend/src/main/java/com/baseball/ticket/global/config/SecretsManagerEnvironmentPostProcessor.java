package com.baseball.ticket.global.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SecretsManagerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {

        // getActiveProfiles() 대신 환경변수 직접 확인
        String profiles = environment.getProperty("spring.profiles.active");
        if (profiles == null) {
            profiles = System.getenv("SPRING_PROFILES_ACTIVE");
        }

        if (profiles == null || !profiles.contains("prod")) {
            return;
        }

        SecretsManagerService secretsManager = new SecretsManagerService();
        Map<String, Object> props = new HashMap<>();

        SecretsManagerService.DbSecret db = secretsManager.getDbSecret();
        props.put("DB_HOST",     db.host());
        props.put("DB_NAME",     db.dbname());
        props.put("DB_USERNAME", db.username());
        props.put("DB_PASSWORD", db.password());

        SecretsManagerService.RedisSecret redis = secretsManager.getRedisSecret();
        props.put("REDIS_HOST", redis.host());

        props.put("JWT_SECRET", secretsManager.getJwtSecret());

        environment.getPropertySources()
                .addFirst(new MapPropertySource("awsSecretsManager", props));
    }
}