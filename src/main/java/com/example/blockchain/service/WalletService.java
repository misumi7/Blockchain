package com.example.blockchain.service;

import com.example.blockchain.model.Wallet;
import com.example.blockchain.repository.WalletRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletService {
    private final WalletRepository walletRepository;
    private final List<Wallet> walletList;
    private Wallet currentWallet;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
        Map<byte[], byte[]> wallets = walletRepository.getAllWallets();
        this.walletList = wallets.entrySet().stream()
                .map(entry -> new Wallet(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        this.currentWallet = walletList.isEmpty() ? null : walletList.getFirst();
        createNewWallet();
    }

    public void switchWallet(){
        // by index?
        // to see when developing frontend
    }

    public void createNewWallet(){
        Wallet newWallet = new Wallet();
        walletRepository.saveWallet(newWallet);
        if(currentWallet == null){
            currentWallet = newWallet;
        }
        walletList.add(newWallet);
    }

    public void deleteWallet(Wallet wallet){
        walletRepository.deleteWallet(wallet);
        walletList.remove(wallet);
        if(currentWallet.equals(wallet)){
            currentWallet = walletList.isEmpty() ? null : walletList.getFirst();
        }
    }

    public byte[] getPrivateKey() {
        return currentWallet.getPrivateKeyObject().getEncoded();
    }

    public byte[] getPublicKey() {
        return currentWallet.getPublicKeyObject().getEncoded();
    }

    public Wallet getCurrentWallet() {
        return currentWallet;
    }
}
