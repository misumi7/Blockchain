package com.example.blockchain.controller;


import com.example.blockchain.model.UTXO;
import com.example.blockchain.service.UTXOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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

    @GetMapping(value = "/{publicKey}/{txId}/{outputIndex}")
    public UTXO getUTXO(@PathVariable("publicKey") String publicKey, @PathVariable("txId") String txId, @PathVariable("outputIndex") int outputIndex) {
        return utxoService.getUTXO(publicKey, txId, outputIndex);
    }

    @GetMapping(value = "/{publicKey}")
    public List<UTXO> getUtxoByOwner(@PathVariable("publicKey") String publicKey) {
        return utxoService.getUtxoByOwner(publicKey);
    }
}
