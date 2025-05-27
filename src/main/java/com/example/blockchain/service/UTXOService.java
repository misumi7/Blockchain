package com.example.blockchain.service;

import com.example.blockchain.model.Node;
import com.example.blockchain.model.UTXO;
import com.example.blockchain.repository.UTXORepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UTXOService {
    private final UTXORepository utxoRepository;
    private final NodeService nodeService;

    public UTXOService(UTXORepository utxoRepository, NodeService nodeService) {
        this.utxoRepository = utxoRepository;
        this.nodeService = nodeService;
    }

    public boolean addUTXO(UTXO utxo) {
        return utxoRepository.saveUTXO(utxo);
    }

    public UTXO getUTXO(String publicKey, String txId, int outputIndex){
        return utxoRepository.getUTXO(publicKey, txId, outputIndex);
    }

    public Map<String, UTXO> getAllUTXOs() {
        return utxoRepository.getAllUTXOs();
    }

    public List<UTXO> getUtxoByOwner(String publicKey) {
        return utxoRepository.getUtxoByOwner(publicKey);
    }

    public boolean deleteUTXO(String publicKey, String txId, int outputIndex) {
        return utxoRepository.deleteUTXO(publicKey, txId, outputIndex);
    }

}
