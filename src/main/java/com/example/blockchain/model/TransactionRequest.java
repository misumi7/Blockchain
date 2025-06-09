package com.example.blockchain.model;

import org.springframework.web.bind.annotation.RequestParam;

public class TransactionRequest {
    private final String senderPublicKey;
    private final String receiverPublicKey;
    private final double amount;
    private final String encryptedPin;

    public TransactionRequest(String senderPublicKey, String receiverPublicKey, double amount, String encryptedPin) {
        this.senderPublicKey = senderPublicKey;
        this.receiverPublicKey = receiverPublicKey;
        this.amount = amount;
        this.encryptedPin = encryptedPin;
    }

    public String getSenderPublicKey() {
        return senderPublicKey;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey;
    }

    public double getAmount() {
        return amount;
    }

    public String getEncryptedPin() {
        return encryptedPin;
    }

    @Override
    public String toString() {
        return "TransactionRequest{" +
                "senderPublicKey='" + senderPublicKey + '\'' +
                ", receiverPublicKey='" + receiverPublicKey + '\'' +
                ", amount=" + amount +
                ", encryptedPin='" + encryptedPin + '\'' +
                '}';
    }
}
