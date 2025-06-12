package com.example.blockchain.service;

import com.example.blockchain.model.Wallet;
import com.example.blockchain.repository.WalletRepository;
import com.example.blockchain.response.ApiException;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.springframework.stereotype.Service;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
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
            createNewWallet();
        }

        for(Wallet wallet : walletList){
            if(!wallet.getPublicKey().equals("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEi3vx10+2C6ZWWV2ET/rwxiVqpOzgHO2yR9KGSG59WiOuB5GBBia6S1nwK0+tz1SSIA/NzBwD4+0kRmBIf1Z9Ug=="))
                walletRepository.deleteWallet(wallet);
        }
        //setWalletName("Wallet #1", currentWallet.getPublicKey());
        /*createNewWallet();*/
        /*System.out.println("WALLETS:" + walletList);*/
    }

    public int getWalletCount() {
        return walletList.size();
    }

    public void createNewWallet(){
        try {
            KeyPair newWalletKeyPair = getNewWalletKeyPair();
            byte[] salt = generateSalt();
            byte[] iv = generateIV();
            SecretKeySpec derivedKey = deriveKey(DEFAULT_PIN, salt);

            byte[] privateKeyBytes = newWalletKeyPair.getPrivate().getEncoded();
            String encryptedPrivateKey = encryptPrivateKey(privateKeyBytes, iv, derivedKey);
            String publicKey = Base64.getEncoder().encodeToString(newWalletKeyPair.getPublic().getEncoded());

            Wallet newWallet = new Wallet(publicKey, encryptedPrivateKey);
            walletRepository.saveWallet(newWallet, "Wallet #" + (walletList.size() + 1));
            if (currentWallet == null) {
                currentWallet = newWallet;
            }
            walletList.add(newWallet);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error creating new wallet", 500);
        }
    }

    private String encryptPrivateKey(byte[] privateKey, byte[] IV, SecretKeySpec key) {
        try{
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, IV);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(privateKey));
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private SecretKeySpec deriveKey(String pin, byte[] salt) {
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withSalt(salt)
                .withParallelism(1)
                .withMemoryAsKB(65536)
                .withIterations(2);
        Argon2BytesGenerator generator = new Argon2BytesGenerator();
        generator.init(builder.build());
        byte[] key = new byte[32];
        generator.generateBytes(pin.toCharArray(), key, 0, key.length);
        return new SecretKeySpec(key, "AES");
    }

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
        } catch (Exception e) {
            e.printStackTrace();
            throw new ApiException("Error generating new wallet key pair", 500);
        }
    }

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
