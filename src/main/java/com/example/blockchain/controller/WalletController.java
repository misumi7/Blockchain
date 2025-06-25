package com.example.blockchain.controller;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.request.UpdatePinRequest;
import com.example.blockchain.request.UpdateWalletNameRequest;
import com.example.blockchain.response.ApiResponse;
import com.example.blockchain.service.TransactionService;
import com.example.blockchain.service.UTXOService;
import com.example.blockchain.service.WalletService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
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

    @GetMapping(value = {"/salt"}, params = {"walletPublicKey"})
    public byte[] getSalt(@RequestParam(value = "walletPublicKey") String walletPublicKey) {
        return walletService.getWalletSalt(walletPublicKey);
    }

    @GetMapping(value = {"/rsa-public-key"})
    public byte[] getRSAPublicKey() {
        return walletService.getRSAPublicKey().getEncoded();
    }

    @GetMapping
    public Map<String, String> getWallets() {
        return walletService.getWallets();
    }

    @GetMapping(value = "/name", params = {"walletPublicKey"})
    public String getWalletName(@RequestParam("walletPublicKey") String walletPublicKey) {
        return walletService.getWalletName(walletPublicKey);
    }

    @GetMapping(value = "/transactions", params = {"walletPublicKey"})
    public List<Transaction> getTransactionsByWallet(@RequestParam("walletPublicKey") String walletPublicKey) {
        return transactionService.getTransactionsByWallet(walletPublicKey);
    }

    @PostMapping
    public ResponseEntity<ApiResponse> createWallet() {
        walletService.createNewWallet();
        return ResponseEntity.ok(new ApiResponse("Wallet created successfully", 200));
    }

    @PatchMapping
    public ResponseEntity<ApiResponse> updateWalletName(@RequestBody UpdateWalletNameRequest updateNameRequest) {
        System.out.println("Update name request: " + updateNameRequest.getWalletName() + ", " + updateNameRequest.getWalletPublicKey());
        walletService.setWalletName(URLDecoder.decode(updateNameRequest.getWalletName()), URLDecoder.decode(updateNameRequest.getWalletPublicKey()));
        return ResponseEntity.ok(new ApiResponse("Wallet name updated successfully", 200));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse> deleteWallet(@RequestParam("walletPublicKey") String walletPublicKey/*, @RequestParam("pin") String encryptedPin*/) {
        //System.out.println("Wallet deleted: " + walletPublicKey);
        walletService.deleteWallet(walletPublicKey/*, encryptedPin*/);
        return ResponseEntity.ok(new ApiResponse("Wallet deleted successfully", 200));
    }

    @PostMapping(value = "/pin")
    public ResponseEntity<ApiResponse> updatePin(@RequestBody UpdatePinRequest updatePinRequest) {
        transactionService.setPin(updatePinRequest.getEncOldPin(), updatePinRequest.getEncNewPin());
        return ResponseEntity.ok(new ApiResponse("PIN updated successfully", 200));
    }

    @GetMapping(value = "/is-default-pin-set")
    public boolean isDefaultPinSet() {
        return transactionService.isDefaultPinSet();
    }
}
