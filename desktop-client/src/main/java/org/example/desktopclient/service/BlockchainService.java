package org.example.desktopclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import org.example.desktopclient.HttpClientProvider;
import org.example.desktopclient.model.Block;
import org.example.desktopclient.model.BlockchainModel;
import org.example.desktopclient.model.Transaction;
import org.example.desktopclient.view.MiningPanel;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class BlockchainService {
    private static final String BASE_URL = "http://localhost:8085/api/blocks";
    private static final HttpClient httpClient = HttpClientProvider.getClient();
    //private final BlockchainModel blockchainModel = BlockchainModel.getInstance();
    private Thread miningThread;
    // If multiple threads will be required, use :
    // ExecutorService executor = Executors.newSingleThreadExecutor();
    // Future<?> miningFuture;
    private AtomicLong miningSessionReward = new AtomicLong(0);

    public void mineBlock(MiningPanel miningPanel, String minerPublicKey, Consumer<String> logCallback, Consumer<Block> mempoolUpdateCallback) {
        miningThread = new Thread("Mining Thread") {
            @Override
            public void run() {
                while(!miningThread.isInterrupted()) {
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
                        updatePerformance(miningPanel, performance);
                        updateBlockMiningDuration(miningPanel, endTime.toEpochMilli() - startTime.toEpochMilli());

                        logCallback.accept(String.format("Block mined successfully: %s", blockToMine.getBlockHash()));

                        /*for(Transaction transaction : blockToMine.getTransactions()) {
                            System.out.println(String.format("Transaction: %s, Amount: %d, Receiver: %s",
                                    transaction.getTransactionId(), transaction.getAmount(), transaction.getReceiverPublicKey()));
                        }*/

                        if (sendMinedBlock(blockToMine)) {
                            logCallback.accept("Mined block successfully sent to the network");
                            miningSessionReward.addAndGet(blockToMine.getTransactions().stream()
                                    .filter(tx -> tx.getReceiverPublicKey().equals(minerPublicKey))
                                    .mapToLong(Transaction::getTransactionFee)
                                    .sum());
                            mempoolUpdateCallback.accept(blockToMine);
                            // TEMP:: TODO:: last transaction should always be the miners reward transaction, but test it, just in case
                            miningSessionReward.addAndGet(blockToMine.getTransactions().getLast().getAmount());
                            updateSessionReward(miningPanel, miningSessionReward.get());
                            //blockchainModel.updateLastBlocks(List.of(blockToMine));
                        }
                        else {
                            logCallback.accept("Failed to send mined block to the network");
                        }
                    }
                    catch (Exception e) {
                        try {
                            Thread.sleep(5000);
                        }
                        catch (InterruptedException interruptedException) {
                            Thread.currentThread().interrupt();
                            //logCallback.accept("Mining thread interrupted.");
                            break;
                        }
                    }
                }
            }
        };
        miningThread.start();
    }

    public CompletableFuture<Block> getBlockToMine(String minerPublicKey, Consumer<String> logCallback){
        String paramName = "minerPublicKey";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/mining/block-to-mine?" + paramName + "=" + URLEncoder.encode(minerPublicKey)))
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
                            e.printStackTrace();
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

    public void stopMining(Consumer<String> logCallback) {
        if (miningThread != null && miningThread.isAlive()) {
            miningThread.interrupt();
            logCallback.accept("Mining stopped.");
        }
    }

    private void updatePerformance(MiningPanel miningPanel, long performance) {
        if (miningPanel != null) {
            miningPanel.updatePerformance(performance);
        }
    }

    public void updateBlockMiningDuration(MiningPanel miningPanel, long duration) {
        if (miningPanel != null) {
            miningPanel.updateBlockMiningDuration(duration);
        }
    }

    public void updateSessionReward(MiningPanel miningPanel, long sessionReward) {
        if (miningPanel != null) {
            miningPanel.updateSessionReward(sessionReward);
        }
    }

    public CompletableFuture<Long> getBlockchainSizeInBytes() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/size"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return Long.parseLong(response.body());
                        }
                        catch (NumberFormatException e) {
                            throw new RuntimeException("Failed to parse blockchain size: " + e.getMessage(), e);
                        }
                    }
                    else {
                        throw new RuntimeException("Failed to get blockchain size: " + response.body());
                    }
                });
    }

    public CompletableFuture<Block> getLastBlock() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/latest"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), Block.class);
                        }
                        catch (Exception e) {
                            throw new RuntimeException("Failed to parse last block: " + e.getMessage(), e);
                        }
                    }
                    else {
                        throw new RuntimeException("Failed to get last block timestamp: " + response.body());
                    }
                });
    }

    public CompletableFuture<Long> getTotalTransactions() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/transaction-count"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            return Long.parseLong(response.body());
                        }
                        catch (NumberFormatException e) {
                            throw new RuntimeException("Failed to parse total transactions: " + e.getMessage(), e);
                        }
                    }
                    else {
                        throw new RuntimeException("Failed to get total transactions: " + response.body());
                    }
                });
    }

    public CompletableFuture<Map<Long, Block>> getLastBlocks(long currentMax, long countToGet) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?from=" + currentMax + "&count=" + countToGet))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), new TypeReference<>() {});
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to parse last blocks: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("Failed to fetch last blocks: HTTP " + response.statusCode());
                    }
                });
    }

    public boolean isBlockchainSync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/sync"))
                .GET()
                .build();
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            System.err.println("Error checking sync status: " + e.getMessage());
            return false;
        }
    }

    public CompletableFuture<Map<Long, Block>> getBlocksFrom(long lastIndex) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "?from=" + lastIndex))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        try {
                            ObjectMapper objectMapper = new ObjectMapper();
                            return objectMapper.readValue(response.body(), new TypeReference<Map<Long, Block>>() {});
                        } catch (JsonProcessingException e) {
                            throw new RuntimeException("Failed to parse blocks: " + e.getMessage(), e);
                        }
                    } else {
                        throw new RuntimeException("Failed to fetch blocks: HTTP " + response.statusCode());
                    }
                });
    }
}
