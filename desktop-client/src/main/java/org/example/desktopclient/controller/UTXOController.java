package org.example.desktopclient.controller;

import javafx.beans.property.SimpleStringProperty;
import org.example.desktopclient.model.WalletsModel;
import org.example.desktopclient.service.UTXOService;

import java.util.HashMap;
import java.util.Map;

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

    public SimpleStringProperty getWalletBalanceProperty(String walletPublicKey) {
        return walletsModel.getWalletBalanceProperty(walletPublicKey);
    }

    public void updateWalletBalances() {
        Map<String, String> walletBalances = new HashMap<>();
        for(String publicKey : walletsModel.getWalletNames().keySet()) {
            //System.out.println("Updating balance for wallet: " + publicKey);
            walletBalances.put(publicKey, String.format("%.5f coins", utxoService.getWalletBalance(publicKey).join()));
        }
        for (String key : walletBalances.keySet()) {
            if (!walletsModel.getWalletBalances().containsKey(key)) {
                walletsModel.getWalletBalances().put(key, new SimpleStringProperty(walletBalances.get(key)));
            }
            else {
                walletsModel.getWalletBalanceProperty(key).set(walletBalances.get(key));
            }
        }
    }

    public void updateWalletBalanceProperty(String walletPublicKey) {
        double walletBalance = utxoService.getWalletBalance(walletPublicKey).join();
        String balance = String.format("%.5f coins", walletBalance);

        if(!walletsModel.getWalletBalances().containsKey(walletPublicKey)) {
            walletsModel.getWalletBalances().put(walletPublicKey, new SimpleStringProperty(balance));
        }
        else {
            walletsModel.getWalletBalanceProperty(walletPublicKey).set(balance);
        }
    }

    public SimpleStringProperty getTotalBalanceProperty() {
        return walletsModel.getTotalBalanceProperty();
    }

    public void updateTotalBalanceProperty() {
        double totalBalance = utxoService.getTotalBalance().join();
        String balance = String.format("%.5f coins", totalBalance);
        walletsModel.setTotalBalance(balance);
    }

}
