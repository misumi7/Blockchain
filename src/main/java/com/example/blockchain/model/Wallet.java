package com.example.blockchain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;

public class Wallet {
    private String publicKey;
    private String encryptedPrivateKey;

    private byte[] salt;
    private byte[] iv;

    /*public Wallet() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();

            this.publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            this.privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*public Wallet(byte[] publicKeyBytes, byte[] privateKeyBytes) {
        try {
            // Validation by generating keys from byte arrays
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            KeyFactory.getInstance("EC", "BC").generatePrivate(privateSpec);
            KeyFactory.getInstance("EC", "BC").generatePublic(publicSpec);

            this.publicKey = publicKeyBytes;
            this.privateKey = privateKeyBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    @JsonCreator
    public Wallet(
            @JsonProperty("publicKey") String publicKey,
            @JsonProperty("encryptedPrivateKey") String encryptedPrivateKey,
            @JsonProperty("salt") byte[] salt,
            @JsonProperty("iv") byte[] iv) {
                this.publicKey = publicKey;
                this.encryptedPrivateKey = encryptedPrivateKey;
                this.salt = salt;
                this.iv = iv;
    }

    public String getPublicKey() {
        return publicKey;
    }

    @JsonIgnore
    public byte[] getPublicKeyBytes() {
        return Base64.getDecoder().decode(publicKey);
    }

    @JsonIgnore
    public byte[] getEncryptedPrivateKeyBytes() {
        return Base64.getDecoder().decode(encryptedPrivateKey);
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    /*@JsonIgnore
    public PublicKey getPublicKeyObject() {
        try {
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(getPublicKeyBytes());
            return KeyFactory.getInstance("EC", "BC").generatePublic(publicSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    /*public PrivateKey getPrivateKeyObject() {
        try {
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(getPrivateKeyBytes());
            return KeyFactory.getInstance("EC", "BC").generatePrivate(privateSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    public byte[] getSalt() {
        return salt;
    }

    public void setSalt(byte[] salt) {
        this.salt = salt;
    }

    public byte[] getIv() {
        return iv;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    @JsonProperty("salt")
    public String getSaltBase64() {
        return Base64.getEncoder().encodeToString(salt);
    }

    @JsonProperty("salt")
    public void setSaltBase64(String saltBase64) {
        this.salt = Base64.getDecoder().decode(saltBase64);
    }

    @JsonProperty("iv")
    public String getIvBase64() {
        return Base64.getEncoder().encodeToString(iv);
    }

    @JsonProperty("iv")
    public void setIvBase64(String ivBase64) {
        this.iv = Base64.getDecoder().decode(ivBase64);
    }

    public static String toJson(Wallet wallet){
        try{
            return new ObjectMapper().writeValueAsString(wallet);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public static Wallet fromJson(String json){
        try{
            return new ObjectMapper().readValue(json, Wallet.class);
        }
        catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "publicKey='" + publicKey + '\'' +
                ", encryptedPrivateKey='" + encryptedPrivateKey + '\'' +
                ", salt=" + Arrays.toString(salt) +
                ", iv=" + Arrays.toString(iv) +
                '}';
    }
}
