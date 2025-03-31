package com.example.blockchain.service;

import com.example.blockchain.model.UTXO;
import com.example.blockchain.repository.UTXORepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UTXOService {
    private final UTXORepository utxoRepository;

    public UTXOService(UTXORepository utxoRepository) {
        this.utxoRepository = utxoRepository;
    }

    public boolean addUTXO(UTXO utxo) {
        return utxoRepository.saveUTXO(utxo);
    }

    public UTXO getUTXO(String txId, int outputIndex){
        return utxoRepository.getUTXO(txId, outputIndex);
    }

    public Map<String, UTXO> getAllUTXOs() {
        return utxoRepository.getAllUTXOs();
    }

    public Map<String, UTXO> getUtxoByOwner(String publicKey) {
        return utxoRepository.findUtxoByOwner(publicKey);
    }
}
