package org.example.desktopclient.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import org.example.desktopclient.model.TableTransaction;
import org.example.desktopclient.model.Transaction;
import org.example.desktopclient.model.TransactionsModel;
import org.example.desktopclient.model.WalletsModel;
import org.example.desktopclient.service.WalletService;
import org.example.desktopclient.view.SideMenu;
import org.example.desktopclient.view.MiningPanel;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WalletController {
    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    private static WalletController instance;
    private final WalletService walletService = new WalletService();
    private final WalletsModel walletsModel = WalletsModel.getInstance();

    private WalletController() {
        updateWalletNames();
    }

    public void updateWalletNames() {
        Map<String, String> walletNames = walletService.getWalletNames().join();
        for (String key : walletNames.keySet()) {
            if (!walletsModel.getWalletNames().containsKey(key)) {
                walletsModel.getWalletNames().put(key, new SimpleStringProperty(walletNames.get(key)));
            }
            else {
                walletsModel.getWalletNameProperty(key).set(walletNames.get(key));
            }
        }
    }

    /*public void updateWalletNames(MiningPanel miningPanel, SideMenu sideMenu){
        Map<String, String> walletNames = walletService.getWalletNames().join();
        miningPanel.setWalletNames(walletNames);
        sideMenu.setWalletNames(walletNames);
    }*/

    public void updateWalletName(String walletPublicKey) {
        String walletName = walletService.getWalletName(walletPublicKey).join();
        walletsModel.setWalletName(walletPublicKey, walletName);
    }

    public static WalletController getInstance() {
        if (instance == null) {
            instance = new WalletController();
        }
        return instance;
    }

    public WalletsModel getWalletsModel() {
        return walletsModel;
    }

    public List<TableTransaction> getWalletTransactions() {
        return walletsModel.getTableTransactions();
    }

    public void updateWalletTransactions(String walletPublicKey) {
        List<Transaction> walletTransactions = walletService.getWalletTransactions(walletPublicKey).join();
        walletsModel.setTableTransactions(walletTransactions.stream().map((t) -> {
            return new TableTransaction(Instant.ofEpochMilli(t.getTimeStamp()).atZone(ZoneId.systemDefault()).format(formatter), t.getAmount() / 100_000_000.0, t.getStatus(), t.getTransactionId());
        }).collect(Collectors.toList()));
    }


}
