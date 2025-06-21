package org.example.desktopclient.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class TransactionRequest {
    private String senderPublicKey;
    private String receiverPublicKey;
    private double amount;
    private String encryptedPin;

    @JsonCreator
    public TransactionRequest(@JsonProperty("senderPublicKey") String senderPublicKey,
                              @JsonProperty("receiverPublicKey") String receiverPublicKey,
                              @JsonProperty("amount") double amount,
                              @JsonProperty("encryptedPin") String encryptedPin) {
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
