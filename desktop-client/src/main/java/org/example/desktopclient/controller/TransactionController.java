package org.example.desktopclient.controller;

import org.example.desktopclient.model.Transaction;
import org.example.desktopclient.model.TransactionsModel;
import org.example.desktopclient.service.TransactionService;
import org.example.desktopclient.service.WalletService;

import javax.crypto.spec.SecretKeySpec;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

public class TransactionController {
    private static TransactionController instance;
    public final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm:ss");
    private TransactionsModel transactionsModel = TransactionsModel.getInstance();
    private TransactionService transactionService = TransactionService.getInstance();
    private WalletService walletService = WalletService.getInstance();

    private TransactionController() {}

    public static TransactionController getInstance() {
        if (instance == null) {
            instance = new TransactionController();
        }
        return instance;
    }

    public void updateTransactionDetails(String transactionId) {
        Transaction transaction = transactionService.getTransactionDetails(transactionId).join();
        transactionsModel.updateTransactionDetails(transaction);
    }

    public Transaction getDisplayedTransaction() {
        return transactionsModel.getDisplayedTransaction();
    }

    public boolean createTransaction(String senderPublicKey, String receiverPublicKey, double amount, String pin) {
        if (senderPublicKey == null || receiverPublicKey == null || amount <= 0 || pin == null) {
            return false;
        }

        byte[] rsaPublicKey = walletService.getRSAPublicKey().join();// change pin to its derived version
        byte[] salt = walletService.getWalletSalt(senderPublicKey).join();
        SecretKeySpec derivedKey = walletService.deriveKey(pin, salt);
        String encryptedAesKey = walletService.encryptAesKey(derivedKey.getEncoded(), rsaPublicKey);
        return transactionService.createTransaction(senderPublicKey, receiverPublicKey, amount, encryptedAesKey).join();
    }
}
