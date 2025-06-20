package org.example.desktopclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.desktopclient.HttpClientProvider;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UTXOService {
    private static final String BASE_URL = "http://localhost:8085/api/utxo";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

    public CompletableFuture<Double> getWalletBalance(String walletPublicKey) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/balance?walletPublicKey=" + URLEncoder.encode(walletPublicKey)))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), Double.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse response body: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("Failed to fetch wallet balance: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Double> getTotalBalance() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/balance/total"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), Double.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse response body: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("Failed to fetch total balance: " + response.statusCode());
                    }
                });
    }
}
