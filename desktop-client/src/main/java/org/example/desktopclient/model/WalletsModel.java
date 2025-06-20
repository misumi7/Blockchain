package org.example.desktopclient.model;

import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletsModel {
    private static WalletsModel instance;
    private final Map<String, SimpleStringProperty> walletNames = new HashMap<>();
    private SimpleStringProperty walletBalance = new SimpleStringProperty("0.00");
    private SimpleStringProperty totalBalance = new SimpleStringProperty("0.00");
    private List<TableTransaction> tableTransactions = new ArrayList<>();

    private WalletsModel() {}

    public List<TableTransaction> getTableTransactions() {
        return tableTransactions;
    }

    public void setTableTransactions(List<TableTransaction> tableTransactions) {
        this.tableTransactions = tableTransactions;
    }

    public SimpleStringProperty getTotalBalanceProperty() {
        return totalBalance;
    }

    public void setTotalBalance(String balance) {
        this.totalBalance.set(balance);
    }

    public SimpleStringProperty getWalletBalanceProperty() {
        return walletBalance;
    }

    public void setWalletBalance(String balance) {
        this.walletBalance.set(balance);
    }

    public SimpleStringProperty getWalletNameProperty(String walletPublicKey) {
        return walletNames.get(walletPublicKey);
    }

    public String getWalletName(String walletPublicKey) {
        return getWalletNameProperty(walletPublicKey).get();
    }

    public void setWalletName(String walletPublicKey, String walletName) {
        getWalletNameProperty(walletPublicKey).set(walletName);
    }

    public Map<String, SimpleStringProperty> getWalletNames() {
        return walletNames;
    }

    public static WalletsModel getInstance() {
        if (instance == null) {
            instance = new WalletsModel();
        }
        return instance;
    }
}
