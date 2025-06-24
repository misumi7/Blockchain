package org.example.desktopclient.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WalletsModel {
    private static WalletsModel instance;
    private final Map<String, SimpleStringProperty> walletNames = new HashMap<>();
    private final Map<String, SimpleStringProperty> walletBalances = new HashMap<>();
    private final SimpleStringProperty totalBalance = new SimpleStringProperty("0.00");
    private List<TableTransactionInfo> tableTransactions = new ArrayList<>();
    private SimpleIntegerProperty notificationCount = new SimpleIntegerProperty(0);

    private WalletsModel() {}

    public int getNotificationCount() {
        return notificationCount.get();
    }

    public SimpleIntegerProperty notificationCountProperty() {
        return notificationCount;
    }

    public void setNotificationCount(int notificationCount) {
        this.notificationCount.set(notificationCount);
    }

    public List<TableTransactionInfo> getTableTransactions() {
        return tableTransactions;
    }

    public void setTableTransactions(List<TableTransactionInfo> tableTransactions) {
        this.tableTransactions = tableTransactions;
    }

    public SimpleStringProperty getTotalBalanceProperty() {
        return totalBalance;
    }

    public void setTotalBalance(String balance) {
        this.totalBalance.set(balance);
    }

    public Map<String, SimpleStringProperty> getWalletBalances() {
        return walletBalances;
    }

    public SimpleStringProperty getWalletBalanceProperty(String walletPublicKey) {
        return walletBalances.get(walletPublicKey);
    }

    public void setWalletBalance(String walletPublicKey, String balance) {
        this.walletBalances.get(walletPublicKey).set(balance);
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
