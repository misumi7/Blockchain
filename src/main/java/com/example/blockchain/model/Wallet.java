package com.example.blockchain.model;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;

public class Wallet {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Wallet() {
        try {
            //Security.addProvider(new BouncyCastleProvider());
            // Use Elliptic Curves from Bouncy Castle
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyGen    .generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            //System.out.println(this.publicKey + "\n" + this.privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Wallet(byte[] publicKeyBytes, byte[] privateKeyBytes) {
        try {
            X509EncodedKeySpec publicSpec = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey = KeyFactory.getInstance("EC", "BC").generatePublic(publicSpec);

            PKCS8EncodedKeySpec privateSpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            this.privateKey = KeyFactory.getInstance("EC", "BC").generatePrivate(privateSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getPublicKey() {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public String getPrivateKey() {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    public PublicKey getPublicKeyObject() {
        return publicKey;
    }

    public PrivateKey getPrivateKeyObject() {
        return privateKey;
    }

    @Override
    public String toString() {
        return "Wallet{" +
                "\n\tpublicKey=||" + getPublicKey() +
                "\n\tprivateKey=" + getPrivateKey() +
                "\n}";
    }
}
