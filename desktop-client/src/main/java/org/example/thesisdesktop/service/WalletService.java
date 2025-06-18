package org.example.thesisdesktop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.thesisdesktop.HttpClientProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WalletService {
    private static final String BASE_URL = "http://localhost:8085/api/wallets";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

    public CompletableFuture<Map<String, String>> getWalletNames(){
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});
                        }
                        catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse response body: " + e.getMessage(), e);
                        }
                    }
                    else {
                        throw new RuntimeException("Failed to fetch wallet names: " + response.statusCode());
                    }
                });
    }
}
