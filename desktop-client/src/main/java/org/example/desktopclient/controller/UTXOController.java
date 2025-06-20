package org.example.desktopclient.controller;

import javafx.beans.property.SimpleStringProperty;
import org.example.desktopclient.model.WalletsModel;
import org.example.desktopclient.service.UTXOService;

public class UTXOController {
    private WalletsModel walletsModel = WalletsModel.getInstance();
    private final UTXOService utxoService = new UTXOService();
    private static UTXOController instance;

    private UTXOController() {}

    public static UTXOController getInstance() {
        if (instance == null) {
            instance = new UTXOController();
        }
        return instance;
    }

    public SimpleStringProperty getWalletBalanceProperty() {
        return walletsModel.getWalletBalanceProperty();
    }

    public void updateWalletBalanceProperty(String walletPublicKey) {
        double walletBalance = utxoService.getWalletBalance(walletPublicKey).join();
        String balance = walletBalance + " coins";
        walletsModel.setWalletBalance(balance);
    }

    public SimpleStringProperty getTotalBalanceProperty() {
        return walletsModel.getTotalBalanceProperty();
    }

    public void updateTotalBalanceProperty() {
        double totalBalance = utxoService.getTotalBalance().join();
        String balance = totalBalance + " coins";
        walletsModel.setTotalBalance(balance);
    }

}
