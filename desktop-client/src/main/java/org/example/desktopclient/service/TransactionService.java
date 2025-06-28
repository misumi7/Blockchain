package org.example.desktopclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.desktopclient.HttpClientProvider;
import org.example.desktopclient.model.Block;
import org.example.desktopclient.model.Transaction;
import org.example.desktopclient.model.TransactionRequest;
import org.example.desktopclient.model.TransactionStatus;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PublicKey;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TransactionService {
    private static TransactionService instance;
    private static final String BASE_URL = "http://localhost:8085/api/transactions";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

    private TransactionService() {}

    public static TransactionService getInstance() {
        if (instance == null) {
            instance = new TransactionService();
        }
        return instance;
    }

    public CompletableFuture<Transaction> getTransactionDetails(String transactionId) {
        String url = BASE_URL + "/" + transactionId;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(responseBody -> {
                    try {
                        ObjectMapper objectMapper = new ObjectMapper();
                        return objectMapper.readValue(responseBody, Transaction.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Failed to parse transaction details", e);
                    }
                });
    }

    public CompletableFuture<Boolean> createTransaction(String senderPublicKey, String receiverPublicKey, double amount, String encryptedPin, Consumer<String> onError) {
        TransactionRequest transactionRequest = new TransactionRequest(senderPublicKey, receiverPublicKey, amount, encryptedPin);

        ObjectMapper objectMapper = new ObjectMapper();
        String body;
        try {
            body = objectMapper.writeValueAsString(transactionRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize transaction", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/create"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    boolean isSuccess = response.statusCode() == 200;
                    if(!isSuccess) {
                        // map the error message from the returned json
                        try {
                            ObjectMapper errorMapper = new ObjectMapper();
                            Map<String, String> errorResponse = errorMapper.readValue(response.body(), new TypeReference<Map<String, String>>() {});
                            onError.accept("Failed to create transaction: " + errorResponse.get("error"));
                        }
                        catch (JsonProcessingException e) {
                            e.printStackTrace();
                        }
                    }
                    return isSuccess;
                });
    }

    public CompletableFuture<Integer> getFeeAmount() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/fee"))
                .GET()
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return Integer.parseInt(response.body());
                    }
                    else {
                        throw new RuntimeException("Failed to fetch fee amount: " + response.statusCode());
                    }
                });
    }
}
