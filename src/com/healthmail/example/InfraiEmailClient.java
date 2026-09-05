package com.healthmail.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public final class InfraiEmailClient {
    // Domain call represented here: infrai.email.send
    private final InfraiProperties properties;
    private final HttpClient http;

    public InfraiEmailClient(InfraiProperties properties) {
        this.properties = properties;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    public String send(String to, String subject, String html) throws Exception {
        String body = "{\"to\":\"" + json(to) + "\",\"subject\":\"" + json(subject) + "\",\"html\":\"" + json(html) + "\"}";
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.baseUrl() + "/v1/email/send"))
                    .header("Authorization", "Bearer " + properties.apiKey())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(20)).POST(HttpRequest.BodyPublishers.ofString(body)).build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":true")) return extract(envelope, "message_id");
            if (response.statusCode() == 429 && attempt < 2) {
                long delay = retryDelay(response, attempt);
                Thread.sleep(delay);
                continue;
            }
            throw new IllegalStateException("Infrai email rejected: " + extractError(envelope));
        }
        throw new IllegalStateException("email delivery did not complete");
    }

    private long retryDelay(HttpResponse<String> response, int attempt) {
        try { return Long.parseLong(response.headers().firstValue("Retry-After").orElse("0")) * 1000L; }
        catch (NumberFormatException ignored) { return 250L * (1L << attempt); }
    }
    private static String extract(String json, String key) {
        String marker = "\"" + key + "\":\""; int start = json.indexOf(marker);
        if (start < 0) return "unknown"; start += marker.length(); int end = json.indexOf('"', start); return json.substring(start, end);
    }
    private static String extractError(String json) { return extract(json, "code"); }
    private static String json(String value) { return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n"); }
}
