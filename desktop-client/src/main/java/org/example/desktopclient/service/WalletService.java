package org.example.desktopclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.desktopclient.HttpClientProvider;
import org.example.desktopclient.model.Transaction;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
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

    public CompletableFuture<String> getWalletName(String walletPublicKey) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/name?walletPublicKey=" + URLEncoder.encode(walletPublicKey)))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), String.class);
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse response body: " + e.getMessage(), e);
                        }
                    }
                    else {
                        throw new RuntimeException("Failed to update wallet name: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<List<Transaction>> getWalletTransactions(String walletPublicKey) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/transactions?walletPublicKey=" + URLEncoder.encode(walletPublicKey)))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), new TypeReference<List<Transaction>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse response body: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("Failed to fetch wallet transactions: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Boolean> setWalletName(String walletPublicKey, String newName) {
        String body = """
             {
                  "walletPublicKey": "%s",
                  "walletName": "%s"
             }
             """.formatted(URLEncoder.encode(walletPublicKey), URLEncoder.encode(newName));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200){
                        return true;
                    }
                    else {
                        throw new RuntimeException("Failed to update wallet name: " + response.body());
                    }
                });
    }

    public CompletableFuture<Boolean> deleteWallet(String publicKey) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?walletPublicKey=" + URLEncoder.encode(publicKey)))
                .DELETE()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    return response.statusCode() == 200;
                });
    }

    public CompletableFuture<Boolean> createWallet() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> response.statusCode() == 200);
    }
}
