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
    private byte[] publicKey;
    private byte[] privateKey;

    public Wallet() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();

            this.privateKey = keyPair.getPrivate().getEncoded();
            this.publicKey = keyPair.getPublic().getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Wallet(byte[] publicKeyBytes, byte[] privateKeyBytes) {
        try {
            this.publicKey = publicKeyBytes;
            this.privateKey = privateKeyBytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(publicKey);
    }

    public byte[] getPublicKeyBytes() {
        return publicKey;
    }

    public byte[] getPrivateKeyBytes() {
        return privateKey;
    }

    public String getPrivateKey() {
        return Base64.getEncoder().encodeToString(privateKey);
    }

    public PublicKey getPublicKeyObject() {
        try {
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(this.publicKey);
            return KeyFactory.getInstance("EC", "BC").generatePublic(publicSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public PrivateKey getPrivateKeyObject() {
        try {
            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(this.privateKey);
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
