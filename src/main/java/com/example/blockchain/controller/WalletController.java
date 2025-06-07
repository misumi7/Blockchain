package com.example.blockchain.controller;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.service.TransactionService;
import com.example.blockchain.service.UTXOService;
import com.example.blockchain.service.WalletService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping(path = "api/wallets")
public class WalletController {
    private WalletService walletService;
    private TransactionService transactionService;

    public WalletController(WalletService walletService, TransactionService transactionService) {
        this.walletService = walletService;
        this.transactionService = transactionService;
    }

    @GetMapping
    public Map<String, String> getWallets() {
        return walletService.getWallets();
    }

    @GetMapping("/{walletPublicKey}/name")
    public String getWalletName(@PathVariable("walletPublicKey") String walletPublicKey) {
        return walletService.getWalletName(walletPublicKey);
    }

    @GetMapping(value = "/{walletPublicKey}/transactions")
    public List<Transaction> getTransactionsByWallet(@PathVariable("walletPublicKey") String walletPublicKey) {
        return transactionService.getTransactionsByWallet(walletPublicKey);
    }
}
