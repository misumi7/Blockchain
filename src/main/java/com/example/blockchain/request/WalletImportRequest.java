package com.example.blockchain.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WalletImportRequest {
    private String publicKey;
    private String encryptedPrivateKey;
    private String salt;
    private String iv;
    private String walletName;

    @JsonCreator
    public WalletImportRequest(
            @JsonProperty("publicKey") String publicKey,
            @JsonProperty("encryptedPrivateKey") String encryptedPrivateKey,
            @JsonProperty("salt") String salt,
            @JsonProperty("iv") String iv,
            @JsonProperty("walletName") String walletName) {
        this.publicKey = publicKey;
        this.encryptedPrivateKey = encryptedPrivateKey;
        this.salt = salt;
        this.iv = iv;
        this.walletName = walletName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getEncryptedPrivateKey() {
        return encryptedPrivateKey;
    }

    public void setEncryptedPrivateKey(String encryptedPrivateKey) {
        this.encryptedPrivateKey = encryptedPrivateKey;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }
}
