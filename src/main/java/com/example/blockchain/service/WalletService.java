package com.example.blockchain.service;

import com.example.blockchain.model.Wallet;
import com.example.blockchain.repository.WalletRepository;
import com.example.blockchain.response.ApiException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.HashMap;
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
        Map<String, String> wallets = walletRepository.getAllWallets();
        this.walletList = wallets.entrySet().stream()
                .map(e -> new Wallet(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
        this.currentWallet = walletList.isEmpty() ? null : walletList.getLast();

    }

    @PostConstruct
    public void init() {
        if(walletList.isEmpty()) {
            createNewWallet();
        }
        //setWalletName("Wallet #1", currentWallet.getPublicKey());
        /*createNewWallet();*/
        /*System.out.println("WALLETS:" + walletList);*/
    }

    public int getWalletCount() {
        return walletList.size();
    }

    public void switchWallet(){
        // by index?
        // to see when developing frontend
    }

    public void createNewWallet(){
        Wallet newWallet = new Wallet();
        walletRepository.saveWallet(newWallet, "Wallet #" + (walletList.size() + 1));
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

    private byte[] encryptPrivateKey(byte[] privateKey, String IV, String key) {

        return Base64.getEncoder().encode(privateKey);
    }

    public byte[] getPrivateKey() {
        return currentWallet.getPrivateKeyBytes();
    }

    public Map<String, String> getWallets() {
        Map<String, String> wallets = new HashMap<>();
        for (Wallet wallet : walletList) {
            wallets.put(wallet.getPublicKey(), walletRepository.getWalletName(wallet.getPublicKeyBytes()));
        }
        return wallets;
    }

    public byte[] getPublicKey() {
        return currentWallet.getPublicKeyBytes();
    }

    public Wallet getCurrentWallet() {
        return currentWallet;
    }

    public void addWallet(Wallet wallet) {
        walletRepository.saveWallet(wallet, "Wallet #" + (walletList.size() + 1));
        walletList.add(wallet);
        if(currentWallet == null){
            currentWallet = wallet;
        }
    }

    public void setWalletName(String name, String walletPublicKey) {
        for (Wallet wallet : walletList) {
            if (wallet.getPublicKey().equals(walletPublicKey)) {
                walletRepository.saveWallet(wallet, name);
                break;
            }
        }
    }

    public List<Wallet> getUserWallets() {
        return walletList;
    }

    public String getWalletName(String walletPublicKey) {
        String walletName = walletRepository.getWalletName(walletPublicKey);
        if(walletName != null) {
            return walletName;
        }
        throw new ApiException("Wallet name not found", 400);
    }
}
