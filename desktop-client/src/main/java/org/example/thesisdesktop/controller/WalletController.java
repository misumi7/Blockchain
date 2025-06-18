package org.example.thesisdesktop.controller;

import org.example.thesisdesktop.service.WalletService;
import org.example.thesisdesktop.view.LeftMenu;
import org.example.thesisdesktop.view.Node;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class WalletController {
    private static WalletController instance;
    private final WalletService walletService = new WalletService();

    private WalletController() {}

    public void updateWalletNames(Node node, LeftMenu leftMenu){
        Map<String, String> walletNames = walletService.getWalletNames().join();
        node.setWalletNames(walletNames);
        leftMenu.setWalletNames(walletNames);
    }

    public static WalletController getInstance() {
        if (instance == null) {
            instance = new WalletController();
        }
        return instance;
    }
}
