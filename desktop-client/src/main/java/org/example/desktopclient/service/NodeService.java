package org.example.desktopclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.desktopclient.HttpClientProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class NodeService {
    private static NodeService instance;
    private static final String BASE_URL = "http://localhost:8085/api/nodes";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

    private NodeService() {}

    public static NodeService getInstance() {
        if (instance == null) {
            instance = new NodeService();
        }
        return instance;
    }

    public CompletableFuture<Integer> getConnectionsCount() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/peers/count"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return Integer.parseInt(response.body());
                    }
                    else {
                        throw new RuntimeException("Failed to fetch connections count: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Long> getMempoolSizeInBytes() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/mempool/size"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return Long.parseLong(response.body());
                    }
                    else {
                        throw new RuntimeException("Failed to fetch mempool size: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<String> getMempoolTransactionCount() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/mempool/count"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        //System.out.println("Response: " + response.body());
                        return response.body();
                    }
                    else {
                        throw new RuntimeException("Failed to fetch mempool transaction count: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Set<String>> getNeighbours() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/peers"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        // Assuming the response is a JSON array of strings
                        try {
                            return new ObjectMapper().readValue(response.body(), new TypeReference<Set<String>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse neighbours", e);
                        }
                    } else {
                        throw new RuntimeException("Failed to fetch neighbours: " + response.statusCode());
                    }
                });
    }

    public boolean getPeerStatus(String peer) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/ping?addr=" + peer))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
