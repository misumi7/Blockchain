package org.example.thesisdesktop.service;

import org.example.thesisdesktop.HttpClientProvider;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class NodeService {
    private static final String BASE_URL = "http://localhost:8085/api/nodes";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

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
                    } else {
                        throw new RuntimeException("Failed to fetch mempool transaction count: " + response.statusCode());
                    }
                });
    }
}
