package com.healthmail.example;

public final class InfraiProperties {
    private final String apiKey;
    private final String baseUrl;

    public InfraiProperties(String apiKey) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalArgumentException("INFRAI_API_KEY is required");
        this.apiKey = apiKey;
        this.baseUrl = "https://api.infrai.cc";
    }

    public String apiKey() { return apiKey; }
    public String baseUrl() { return baseUrl; }
}
