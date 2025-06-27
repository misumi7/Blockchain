package org.example.desktopclient.controller;

import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import org.example.desktopclient.model.TableTransactionInfo;
import org.example.desktopclient.model.Transaction;
import org.example.desktopclient.model.WalletsModel;
import org.example.desktopclient.service.WalletService;
import org.example.desktopclient.view.MiningPanel;
import org.example.desktopclient.view.Settings;
import org.example.desktopclient.view.SideMenu;

import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WalletController {
    public Settings settings = Settings.getInstance();
    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    private static WalletController instance;
    private final WalletService walletService = WalletService.getInstance();
    private final WalletsModel walletsModel = WalletsModel.getInstance();

    private WalletController() {
        updateWalletNames();
        settings.bindNotificationCount(getNotificationCountProperty());
        if(walletService.isDefaultPinSet().join()){
            Platform.runLater(() -> {
                createNewNotification("You're currently using the default PIN code. For your security, we recommend updating it.", true);
                createNewNotification("New user detected. Welcome!", false);
            });
            // Testing the notification system::
            /*new Thread(() -> {
                try {
                    for (int i = 0; i < 12; ++i) {
                        Thread.sleep(1000);
                        Platform.runLater(() -> {
                            createNewNotification("Please update your PIN code to enhance security.", true);
                        });
                    }
                }
                catch(Exception e){
                        e.printStackTrace();
                }
            }).start();*/
        }
    }

    public void updatePin(String oldPin, String newPin){
        updateWalletNames();
        byte[] rsaPublicKey = walletService.getRSAPublicKey().join();

        for(String walletPublicKey : walletsModel.getWalletNames().keySet()){
            byte[] salt = walletService.getWalletSalt(walletPublicKey).join();
            SecretKeySpec oldDerivedKey = walletService.deriveKey(oldPin, salt);
            SecretKeySpec newDerivedKey = walletService.deriveKey(newPin, salt);
            String encryptedOldKey = walletService.encryptAesKey(oldDerivedKey.getEncoded(), rsaPublicKey);
            String encryptedNewKey = walletService.encryptAesKey(newDerivedKey.getEncoded(), rsaPublicKey);
            walletService.updatePin(walletPublicKey, encryptedOldKey, encryptedNewKey);

            // TEMP::
        }
        createNewNotification("PIN code updated successfully.", false);
    }

    public void createNewNotification(String message, boolean isAlert) {
        settings.addNewNotification(message, isAlert);
        incNotificationCount();
    }

    public void incNotificationCount() {
        walletsModel.setNotificationCount(walletsModel.getNotificationCount() + 1);
    }

    public SimpleIntegerProperty getNotificationCountProperty() {
        return walletsModel.notificationCountProperty();
    }

    public void decNotificationCount() {
        if(walletsModel.getNotificationCount() > 0) {
            walletsModel.setNotificationCount(walletsModel.getNotificationCount() - 1);
        }
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

    public List<TableTransactionInfo> getWalletTransactions() {
        return walletsModel.getTableTransactions();
    }

    public void updateWalletTransactions(String walletPublicKey) {
        List<Transaction> walletTransactions = walletService.getWalletTransactions(walletPublicKey).join();
        walletsModel.setTableTransactions(walletTransactions.stream().map((t) -> {
            return new TableTransactionInfo(Instant.ofEpochMilli(t.getTimeStamp()).atZone(ZoneId.systemDefault()).format(formatter), t.getAmount() / 100_000_000.0, t.getStatus(), t.getTransactionId());
        }).collect(Collectors.toList()));
    }

    public Map<String, SimpleStringProperty> getWalletNames() {
        return walletsModel.getWalletNames();
    }

    public void setWalletName(String walletPublicKey, String newName) {
        if(walletService.setWalletName(walletPublicKey, newName).join()) {
            walletsModel.setWalletName(walletPublicKey, newName);
        }
        else {
            // TEMP:: maybe show an error notification
            throw new RuntimeException("Failed to set wallet name");
        }
    }

    public void deleteWallet(String publicKey) {
        if(walletService.deleteWallet(publicKey).join()) {
            System.out.println("Wallet deleted successfully: " + publicKey);
            walletsModel.getWalletNames().remove(publicKey);
            walletsModel.getWalletBalances().remove(publicKey);
            SideMenu.getInstance().updateSideMenuWalletList();
            //walletsModel.getTableTransactions().removeIf(t -> t.getSenderPublicKey().equals(publicKey));
        }
        else {
            // TEMP:: show an error notification
            throw new RuntimeException("Failed to delete wallet");
        }
    }

    public boolean createWallet() {
        return walletService.createWallet().join();
    }

    public String downloadWallet(String publicKey) {
        return walletService.downloadWallet(publicKey).join();
    }

    public void importWallet(File file) {
        walletService.createWallet(file).join();
        updateWalletNames();
        SideMenu.getInstance().updateSideMenuWalletList();
        MiningPanel.getInstance().updateComboBoxOptions();
    }
}
