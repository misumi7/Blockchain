package com.example.blockchain.service;

import com.example.blockchain.model.Node;
import com.example.blockchain.model.UTXO;
import com.example.blockchain.model.Wallet;
import com.example.blockchain.repository.UTXORepository;
import com.example.blockchain.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class UTXOService {
    private final UTXORepository utxoRepository;
    private final WalletService walletService;
    private final NodeService nodeService;

    public UTXOService(UTXORepository utxoRepository, NodeService nodeService, WalletService walletService) {
        this.walletService = walletService;
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

    public void deleteAllUTXO() {
        utxoRepository.deleteAllUTXO();
    }

    public double getWalletBalance(String walletPublicKey) {
        List<UTXO> utxos = getUtxoByOwner(walletPublicKey);
        double balance = 0;
        for (UTXO utxo : utxos) {
            balance += (double) utxo.getAmount() / 100_000_000L;
        }
        return balance;    }

    public double getTotalBalance() {
        List<Wallet> userWallets = walletService.getUserWallets();
        double totalBalance = 0;
        for (Wallet wallet : userWallets) {
            totalBalance += getWalletBalance(wallet.getPublicKey());
        }
        return totalBalance;
    }

    public long getInputsRequired(String walletPublicKey, long amount) {
        List<UTXO> utxos = getUtxoByOwner(walletPublicKey);
        long totalAmount = 0;
        long inputsRequired = 0;

        for (UTXO utxo : utxos) {
            totalAmount += utxo.getAmount();
            inputsRequired++;
            if (totalAmount >= amount) {
                break;
            }
        }

        return inputsRequired;
    }
}
