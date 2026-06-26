package com.baseball.ticket.global.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

public class SecretsManagerService {

    private static final Region REGION = Region.EU_WEST_2;

    private static final String DB_SECRET_NAME    = "kky-prod-db-secret";
    private static final String REDIS_SECRET_NAME = "kky-prod-redis-secret";
    private static final String JWT_SECRET_NAME   = "kky-prod-jwt-secret";

    private final SecretsManagerClient client;
    private final ObjectMapper objectMapper;

    public SecretsManagerService() {
        this.client = SecretsManagerClient.builder()
                .region(REGION)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public DbSecret getDbSecret() {
        String secretString = getSecretValue(DB_SECRET_NAME);
        try {
            JsonNode node = objectMapper.readTree(secretString);
            return new DbSecret(
                    node.get("host").asText(),
                    node.get("port").asInt(),
                    node.get("dbname").asText(),
                    node.get("username").asText(),
                    node.get("password").asText()
            );
        } catch (Exception e) {
            throw new RuntimeException("DB 시크릿 파싱 실패: " + e.getMessage(), e);
        }
    }

    public RedisSecret getRedisSecret() {
        String secretString = getSecretValue(REDIS_SECRET_NAME);
        try {
            JsonNode node = objectMapper.readTree(secretString);
            return new RedisSecret(
                    node.get("host").asText(),
                    node.get("port").asInt()
            );
        } catch (Exception e) {
            throw new RuntimeException("Redis 시크릿 파싱 실패: " + e.getMessage(), e);
        }
    }

    public String getJwtSecret() {
        String secretString = getSecretValue(JWT_SECRET_NAME);
        try {
            JsonNode node = objectMapper.readTree(secretString);
            return node.get("secret").asText();
        } catch (Exception e) {
            throw new RuntimeException("JWT 시크릿 파싱 실패: " + e.getMessage(), e);
        }
    }

    private String getSecretValue(String secretName) {
        GetSecretValueRequest request = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();
        GetSecretValueResponse response = client.getSecretValue(request);
        return response.secretString();
    }

    public record DbSecret(
            String host, int port, String dbname,
            String username, String password) {}

    public record RedisSecret(String host, int port) {}
}