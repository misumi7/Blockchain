package com.example.blockchain.service;

import com.example.blockchain.model.Wallet;
import com.example.blockchain.repository.WalletRepository;
import com.example.blockchain.response.ApiException;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletService {
    public static final String DEFAULT_PIN = "111111";
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
            //createNewWallet();
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

    /*public void createNewWallet(){
        KeyPair newWalletKeyPair = getNewWalletKeyPair();

        String publicKey = Base64.getEncoder().encodeToString(newWalletKeyPair.getPublic().getEncoded());

        byte[] salt = generateSalt();
        byte[] iv = generateIV();
        SecretKeySpec derivedKey = deriveKey(pin, salt);

        Wallet newWallet = new Wallet(publicKey, encryptedPrivateKey);
        walletRepository.saveWallet(newWallet, "Wallet #" + (walletList.size() + 1));
        if(currentWallet == null){
            currentWallet = newWallet;
        }
        walletList.add(newWallet);
    }*/

   /* private SecretKeySpec deriveKey(String pin, byte[] salt) {
        Argon2 argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);
        byte[] key = argon2.hash(2, 65536, 1, pin.toCharArray(), salt); // to search for methods that permits key derivation with custom salt
        return new SecretKeySpec(key, "AES");
    }*/

    private byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private byte[] generateIV() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[12];
        random.nextBytes(salt);
        return salt;
    }

    private KeyPair getNewWalletKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            return keyGen.generateKeyPair();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error generating new wallet key pair", 500);
        }
    }

    /*private String encryptPrivateKey(byte[] privateKey, String IV, String key) {

        //return Base64.getEncoder().encode(privateKey);
    }*/

    public void deleteWallet(Wallet wallet){
        walletRepository.deleteWallet(wallet);
        walletList.remove(wallet);
        if(currentWallet.equals(wallet)){
            currentWallet = walletList.isEmpty() ? null : walletList.getFirst();
        }
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
                return;
            }
        }
        throw new ApiException("Wallet not found", 404);
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
