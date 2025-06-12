package com.example.blockchain.request;

public class UpdateWalletNameRequest {
    private String walletPublicKey;
    private String walletName;

    public UpdateWalletNameRequest(String walletPublicKey, String walletName) {
        this.walletPublicKey = walletPublicKey;
        this.walletName = walletName;
    }

    public String getWalletPublicKey() {
        return walletPublicKey;
    }

    public void setWalletPublicKey(String walletPublicKey) {
        this.walletPublicKey = walletPublicKey;
    }

    public String getWalletName() {
        return walletName;
    }

    public void setWalletName(String walletName) {
        this.walletName = walletName;
    }

    @Override
    public String toString() {
        return "UpdateWalletNameRequest{" +
                "walletPublicKey='" + walletPublicKey + '\'' +
                ", walletName='" + walletName + '\'' +
                '}';
    }
}
