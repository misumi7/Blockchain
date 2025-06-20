package org.example.desktopclient.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.desktopclient.HttpClientProvider;
import org.example.desktopclient.model.Transaction;
import org.example.desktopclient.model.TransactionStatus;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class TransactionService {
    private static final String BASE_URL = "http://localhost:8085/api/transactions";
    private static final HttpClient httpClient = HttpClientProvider.getClient();

}
