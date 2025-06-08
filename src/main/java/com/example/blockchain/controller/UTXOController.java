package com.example.blockchain.controller;


import com.example.blockchain.model.UTXO;
import com.example.blockchain.service.UTXOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping(path = "api/utxo")
public class UTXOController {
    private final UTXOService utxoService;

    @Autowired
    public UTXOController(UTXOService utxoService) {
        this.utxoService = utxoService;
    }

    // For testing purposes
    @PostMapping
    public boolean addUTXO(@RequestBody UTXO utxo) {
        return utxoService.addUTXO(utxo);
    }

    @GetMapping
    public Map<String, UTXO> getAllUTXOs() {
        return utxoService.getAllUTXOs();
    }

    @GetMapping(params = {"publicKey", "txId", "outputIndex"})
    public UTXO getUTXO(@RequestParam("publicKey") String publicKey, @RequestParam("txId") String txId, @RequestParam("outputIndex") int outputIndex) {
        return utxoService.getUTXO(publicKey, txId, outputIndex);
    }

    @GetMapping(params = {"walletPublicKey"})
    public List<UTXO> getUtxoByOwner(@RequestParam("walletPublicKey") String publicKey) {
        return utxoService.getUtxoByOwner(publicKey);
    }

    @GetMapping(value = "/balance", params = {"walletPublicKey"})
    public double getWalletBalance(@RequestParam("walletPublicKey") String walletPublicKey) {
        return utxoService.getWalletBalance(walletPublicKey);
    }

    @GetMapping("/balance/total")
    public double getWalletBalance() {
        return utxoService.getTotalBalance();
    }
}
