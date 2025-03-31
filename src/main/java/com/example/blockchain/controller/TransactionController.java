package com.example.blockchain.controller;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.service.BlockchainService;
import com.example.blockchain.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(path = "api/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public boolean addTransaction(@RequestBody Transaction transaction) {
        return transactionService.saveTransaction(transaction);
    }

    @GetMapping
    public Map<String, Transaction> getTransactions() {
        return transactionService.getTransactions();
    }

    @GetMapping(value = "/{transactionId}")
    public Transaction getTransaction(@PathVariable("transactionId") String transactionId) {
        return transactionService.getTransaction(transactionId);
    }

}
