package org.example.thesisdesktop.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.thesisdesktop.HttpClientProvider;
import org.example.thesisdesktop.model.Block;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class BlockchainService {
    private static final String BASE_URL = "http://localhost:8085/api/blocks";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

    public void mineBlock(String minerPublicKey, Consumer<String> logCallback) {
        try {
            Block blockToMine = getBlockToMine(minerPublicKey, logCallback).join();
            blockToMine.setBlockHash(blockToMine.calculateHash());
            logCallback.accept(String.format("Block to mine received: %s, with %d transactions", blockToMine.getBlockHash(), blockToMine.getTransactions().size()));

            // As long as the nonce starts from 0, we can use it for hash rate performance calculation
            //int totalHashes = 0;
            Instant startTime = Instant.now();
            while (!blockToMine.checkProofOfWork()) {
                blockToMine.incrementNonce();
                blockToMine.setBlockHash(blockToMine.calculateHash());
                //totalHashes++;
                //System.out.println("[MINING] Trying to mine block: " + blockToMine.getBlockHash() + " with nonce: " + blockToMine.getNonce());
            }
            Instant endTime = Instant.now();
            long performance = blockToMine.getNonce() / (endTime.toEpochMilli() - startTime.toEpochMilli()) * 1000;
            logCallback.accept(String.format("Block mined successfully: %s \n\t\tNonce: %d \n\t\tTime: %d ms \n\t\tPerformance: %d h/s",
                    blockToMine.getBlockHash(),
                    blockToMine.getNonce(),
                    endTime.toEpochMilli() - startTime.toEpochMilli(), performance
            ));

            blockToMine.setBlockHash(blockToMine.calculateHash());

            if (sendMinedBlock(blockToMine)) {
                logCallback.accept("Mined block successfully sent to the network");
            }
            else {
                logCallback.accept("Failed to send mined block to the network");
            }
        }
        catch (Exception e){
            throw new RuntimeException("Failed to mine block: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<Block> getBlockToMine(String minerPublicKey, Consumer<String> logCallback){
        String paramName = "minerPublicKey";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/mining/block-to-mine?" + paramName + "=" + minerPublicKey))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), Block.class);
                        }
                        catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse response body: " + e.getMessage(), e);
                        }
                    }
                    else {
                        try {
                            Map<String, String> responseMap = new ObjectMapper().readValue(response.body(), new TypeReference<>() {});
                            logCallback.accept("Failed to get block to mine: " + responseMap.get("error"));
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException(e);
                        }
                        throw new RuntimeException("Failed to get block to mine: " + response.body());
                    }
                });
    }

    private boolean sendMinedBlock(Block block) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(Objects.requireNonNull(Block.toJson(block))))
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        }
        catch (Exception e) {
            System.err.println("Error sending mined block: " + e.getMessage());
            return false;
        }
    }
}
