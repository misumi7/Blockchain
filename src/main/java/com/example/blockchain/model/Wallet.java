package com.example.blockchain.model;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.*;

import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.ECNamedCurveTable;

public class Wallet {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    public Wallet() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            // Use Elliptic Curves from Bouncy Castle
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC", "BC");
            ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
            keyGen.initialize(ecSpec, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
            //System.out.println(this.publicKey + "\n" + this.privateKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
