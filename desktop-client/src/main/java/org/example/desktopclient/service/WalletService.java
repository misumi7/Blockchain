package org.example.desktopclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.desktopclient.HttpClientProvider;
import org.example.desktopclient.model.Transaction;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WalletService {
    private static WalletService instance;
    private static final String BASE_URL = "http://localhost:8085/api/wallets";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

    private WalletService() {}

    public static WalletService getInstance() {
        if (instance == null) {
            instance = new WalletService();
        }
        return instance;
    }

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

    public CompletableFuture<Boolean> createWallet(File file){
        StringBuilder jsonContent = new StringBuilder();
        try(BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while((line = br.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to read wallet file: " + file.getName());
        }
        String body = jsonContent.toString().replace("}", ", \"walletName\": \"%s\"}".formatted(file.getName().replace(".json", "")));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/import"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        throw new RuntimeException("Failed to create wallet: " + response.body());
                    }
                });
    }

    public String encryptPin(String pin, byte[] rsaPublicKey) {
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(rsaPublicKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(spec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);
            byte[] encryptedPin = cipher.doFinal(pin.getBytes());
            return Base64.getEncoder().encodeToString(encryptedPin);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt PIN: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<byte[]> getRSAPublicKey() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/rsa-public-key"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofByteArray())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    } else {
                        throw new RuntimeException("Failed to fetch RSA public key: " + response.statusCode());
                    }
                });
    }

    public CompletableFuture<Boolean> isDefaultPinSet() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/is-default-pin-set"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return Boolean.parseBoolean(response.body());
                    } else {
                        throw new RuntimeException("Failed to check if default PIN is set: " + response.statusCode());
                    }
                });
    }

    public boolean updatePin(String encryptedOldPin, String encryptedNewPin) {
        String body = """
        {
            "oldPin": "%s",
            "newPin": "%s"
        }
        """.formatted(
                URLEncoder.encode(encryptedOldPin, StandardCharsets.UTF_8),
                URLEncoder.encode(encryptedNewPin, StandardCharsets.UTF_8)
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/pin"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return true;
                    } else {
                        throw new RuntimeException("Failed to update PIN: " + response.body());
                    }
                }).join();
    }

    public CompletableFuture<String> downloadWallet(String publicKey) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/wallet?walletPublicKey=" + URLEncoder.encode(publicKey, StandardCharsets.UTF_8)))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return response.body();
                    }
                    else {
                        throw new RuntimeException("Failed to download wallet: " + response.statusCode());
                    }
                });
    }
}
