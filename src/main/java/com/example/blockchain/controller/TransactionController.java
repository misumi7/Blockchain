package com.example.blockchain.controller;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.request.TransactionRequest;
import com.example.blockchain.response.ApiResponse;
import com.example.blockchain.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping(path = "api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping(value = "/add")
    public ResponseEntity<ApiResponse> addTransaction(@RequestBody Transaction transaction) {
        transactionService.saveTransactionToMemPool(transaction);
        return ResponseEntity.ok(new ApiResponse("Transaction added successfully", 200));
    }

    @PostMapping(value = "/create")
    public ResponseEntity<ApiResponse> createTransaction(@RequestBody TransactionRequest transactionRequest) {
        System.out.println("Received transaction request: " + transactionRequest.getEncryptedPin());
        transactionService.createTransaction(transactionRequest);
        return ResponseEntity.ok(new ApiResponse("Transaction created successfully", 200));
    }

    @GetMapping
    public Map<String, Transaction> getTransactions() {
        return transactionService.getTransactions();
    }

    @GetMapping(value = "/fee")
    public short getRecommendedFee() {
        return transactionService.getOptimalFeeRate();
    }

    @GetMapping(value = "/{transactionId}")
    public Transaction getTransaction(@PathVariable("transactionId") String transactionId) {
        return transactionService.getTransaction(transactionId);
    }
}
