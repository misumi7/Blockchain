package org.example.desktopclient.model;

import javafx.beans.property.SimpleStringProperty;

public class TableWalletInfo {
    String publicKey;
    SimpleStringProperty walletName;
    SimpleStringProperty walletBalance;

    public TableWalletInfo(String publicKey, String walletName, String walletBalance) {
        this.publicKey = publicKey;
        this.walletName = new SimpleStringProperty(walletName);
        this.walletBalance = new SimpleStringProperty(walletBalance);
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getWalletName() {
        return walletName.get();
    }

    public void setWalletName(String walletName) {
        this.walletName.set(walletName);
    }

    public SimpleStringProperty getWalletNameProperty() {
        return walletName;
    }

    public String getWalletBalance() {
        return walletBalance.get();
    }

    public void setWalletBalance(String walletBalance) {
        this.walletBalance.set(walletBalance);
    }

    public SimpleStringProperty getWalletBalanceProperty() {
        return walletBalance;
    }
}
