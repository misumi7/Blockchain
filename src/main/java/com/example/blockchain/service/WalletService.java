package com.example.blockchain.service;

import com.example.blockchain.model.Transaction;
import com.example.blockchain.model.TransactionStatus;
import com.example.blockchain.model.Wallet;
import com.example.blockchain.repository.WalletRepository;
import com.example.blockchain.response.ApiException;
import jakarta.annotation.PostConstruct;
import org.bouncycastle.asn1.sec.ECPrivateKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.*;

@Service
public class WalletService {
    public static final String DEFAULT_PIN = "111111";
    public static final String DEFAULT_KS_PASS = "kspassword@0";
    public static final String KEYPAIR_ALIAS = "rsa-keypair";
    private final WalletRepository walletRepository;
    private List<Wallet> walletList;
    private Wallet currentWallet;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
        this.walletList = walletRepository.getAllWallets();
        this.currentWallet = walletList.isEmpty() ? null : walletList.getLast();

        setRSAKeyPair();

        if (this.privateKey == null || this.publicKey == null) {
            try {
                System.out.println("[KEY PAIR GENERATION] Generating new key pair");
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
                keyGen.initialize(2048);
                KeyPair keyPair = keyGen.generateKeyPair();

                Date notBefore = new Date();
                Date notAfter = new Date(notBefore.getTime() + 365 * 24 * 60 * 60 * 1000L); // 1 year

                X500Name certOwner = new X500Name("CN=localUser");
                BigInteger uniqueSerialNumber = BigInteger.valueOf(System.currentTimeMillis());
                ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").build(keyPair.getPrivate());
                X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                        certOwner, uniqueSerialNumber, notBefore, notAfter, certOwner, keyPair.getPublic()
                );
                X509CertificateHolder certHolder = certBuilder.build(signer);
                X509Certificate cert = new JcaX509CertificateConverter()
                        .setProvider("BC")
                        .getCertificate(certHolder);

                KeyStore ks = KeyStore.getInstance("PKCS12");
                ks.load(null, null);
                ks.setKeyEntry(
                        KEYPAIR_ALIAS,
                        keyPair.getPrivate(),
                        DEFAULT_KS_PASS.toCharArray(),
                        new java.security.cert.Certificate[]{cert}
                );
                try (OutputStream os = new FileOutputStream("keystore.p12")) {
                    ks.store(os, DEFAULT_KS_PASS.toCharArray());
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        setRSAKeyPair();
    }

    @PostConstruct
    public void init() {
        if (walletList.isEmpty()) {
            createNewWallet();
        }

        // TEMP::
        /*try {
            walletList.removeIf(wallet -> {
                if(!wallet.getPublicKey().equals("MFYwEAYHKoZIzj0CAQYFK4EEAAoDQgAEi3vx10+2C6ZWWV2ET/rwxiVqpOzgHO2yR9KGSG59WiOuB5GBBia6S1nwK0+tz1SSIA/NzBwD4+0kRmBIf1Z9Ug==")){
                    walletRepository.deleteWallet(wallet);
                    return true;
                }
                return false;
            });
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }*/
        /*createNewWallet();*/
        /*System.out.println("WALLETS:" + walletList);*/
    }

    public void setRSAKeyPair() {
        try (InputStream is = new FileInputStream("keystore.p12")) {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(is, DEFAULT_KS_PASS.toCharArray());
            this.privateKey = (PrivateKey) ks.getKey(KEYPAIR_ALIAS, DEFAULT_KS_PASS.toCharArray());
            this.publicKey = ks.getCertificate(KEYPAIR_ALIAS).getPublicKey();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getWalletCount() {
        return walletList.size();
    }

    // Decrypts the PIN with the private key (RSA)
    /*public void decryptPin(String encryptedPin){
        try{

        }
        catch (Exception e) {

        }
    }*/

    public void createNewWallet(){
        try {
            KeyPair newWalletKeyPair = getNewWalletKeyPair();
            byte[] salt = generateSalt();
            byte[] iv = generateIV();
            SecretKeySpec derivedKey = deriveKey(DEFAULT_PIN, salt);

            byte[] privateKeyBytes = newWalletKeyPair.getPrivate().getEncoded();
            String encryptedPrivateKey = encryptPrivateKey(privateKeyBytes, iv, derivedKey);
            String publicKey = Base64.getEncoder().encodeToString(newWalletKeyPair.getPublic().getEncoded());

            Wallet newWallet = new Wallet(publicKey, encryptedPrivateKey, salt, iv);
            System.out.println(newWallet);
            addWallet(newWallet);
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

    public byte[] decryptPrivateKey(byte[] encPrivateKeyBytes, byte[] IV, SecretKeySpec key) {
        try{
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            GCMParameterSpec spec = new GCMParameterSpec(128, IV);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(encPrivateKeyBytes);
        }  catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    protected SecretKeySpec deriveKey(String pin, byte[] salt) {
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

    // TEMP version!!
    public void deleteWallet(String walletPublicKey){
        Wallet walletToRemove = walletList.stream()
                .filter(wallet -> wallet.getPublicKey().equals(walletPublicKey))
                .findFirst()
                .orElseThrow(() -> new ApiException("Wallet not found", 404));
        walletRepository.deleteWallet(walletToRemove);
        walletList.remove(walletToRemove);
        if(currentWallet.equals(walletToRemove)){
            currentWallet = walletList.isEmpty() ? null : walletList.getFirst();
        }
    }

    public void deleteWallet(String walletPublicKey, String pin){

    }

    public Wallet getWallet(String publicKey) {
        return walletList.stream()
                .filter(wallet -> wallet.getPublicKey().equals(publicKey))
                .findFirst()
                .orElseThrow(() -> new ApiException("Wallet not found", 404));
    }

    public byte[] getPrivateKey() {
        return currentWallet.getEncryptedPrivateKeyBytes();
    }

    public Map<String, String> getWallets() {
        Map<String, String> wallets = new HashMap<>();
        for (Wallet wallet : walletList) {
            wallets.put(wallet.getPublicKey(), walletRepository.getWalletName(wallet.getPublicKeyBytes()));
        }
        return wallets;
    }

    public List<Wallet> getWalletList() {
        return walletList;
    }

    public byte[] getPublicKey() {
        return currentWallet.getPublicKeyBytes();
    }

    public Wallet getCurrentWallet() {
        return currentWallet;
    }

    public void addWallet(Wallet wallet) {
        String publicKey = wallet.getPublicKey();
        walletRepository.saveWallet(wallet, "Wallet #" + publicKey.substring(publicKey.length() - 8, publicKey.length() - 2));
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

    public byte[] getWalletSalt(String walletPublicKey) {
        Wallet wallet = walletList.stream()
                .filter(w -> w.getPublicKey().equals(walletPublicKey))
                .findFirst()
                .orElseThrow(() -> new ApiException("Wallet not found", 404));
        byte[] walletSalt = wallet.getSalt();
        if(walletSalt != null) {
            return walletSalt;
        }
        throw new ApiException("Wallet salt not found", 400);
    }

    public byte[] getWalletIV(String walletPublicKey) {
        Wallet wallet = walletList.stream()
                .filter(w -> w.getPublicKey().equals(walletPublicKey))
                .findFirst()
                .orElseThrow(() -> new ApiException("Wallet not found", 404));
        byte[] walletIV = wallet.getIv();
        if(walletIV != null) {
            return walletIV;
        }
        throw new ApiException("Wallet IV not found", 400);
    }

    public PrivateKey getRSAPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getRSAPublicKey() {
        return this.publicKey;
    }

    public String decryptPin(byte[] ecnPin, PrivateKey privateKey) {
        try {
            /*for(byte b : ecnPin) {
                System.out.println(b);
            }*/
            Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1", new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
            cipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);
            byte[] decryptedBytes = cipher.doFinal(ecnPin);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public void setPin(String encryptedPin, String newEncryptedPin) {
    }
}
