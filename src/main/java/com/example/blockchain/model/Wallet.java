package com.example.blockchain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.IOException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;

public class Wallet {
    private String publicKey;
    private String privateKey;

    public Wallet() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();

            this.privateKey = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            this.publicKey = Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

    public Wallet(String publicKey, String privateKey) {
        try {
            this.publicKey = publicKey;
            this.privateKey = privateKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPublicKey() {
        return publicKey;
    }

    public byte[] getPublicKeyBytes() {
        return Base64.getDecoder().decode(publicKey);
    }

    public byte[] getPrivateKeyBytes() {
        return Base64.getDecoder().decode(privateKey);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKeyObject() {
        try {
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(getPublicKeyBytes());
            return KeyFactory.getInstance("EC", "BC").generatePublic(publicSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public PrivateKey getPrivateKeyObject() {
        try {
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(getPrivateKeyBytes());
            return KeyFactory.getInstance("EC", "BC").generatePrivate(privateSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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

    @Override
    public String toString() {
        return "Wallet{" +
                "\n\tpublicKey=" + getPublicKey() +
                "\n\tprivateKey=" + getPrivateKey() +
                "\n}";
    }
}
