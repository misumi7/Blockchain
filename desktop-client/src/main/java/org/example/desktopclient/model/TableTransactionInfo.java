package org.example.desktopclient.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableTransactionInfo {
    private SimpleStringProperty timeStamp;
    private SimpleDoubleProperty amount;
    private SimpleStringProperty status;
    private SimpleStringProperty transactionId;
    private SimpleStringProperty senderPublicKey;
    private SimpleStringProperty receiverPublicKey;

    public TableTransactionInfo(String timeStamp, double amount, TransactionStatus status, String transactionId, String senderPublicKey, String receiverPublicKey) {
        this.timeStamp = new SimpleStringProperty(timeStamp);
        this.amount = new SimpleDoubleProperty(amount);
        this.status = new SimpleStringProperty(status.toString().substring(0, 1) + status.toString().substring(1).toLowerCase());
        this.transactionId = new SimpleStringProperty(transactionId);
        this.senderPublicKey = new SimpleStringProperty(senderPublicKey);
        this.receiverPublicKey = new SimpleStringProperty(receiverPublicKey);
    }

    public String getTimeStamp() {
        return timeStamp.get();
    }

    public double getAmount() {
        return amount.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getTransactionId() {
        return transactionId.get();
    }

    public SimpleStringProperty timeStampProperty() {
        return timeStamp;
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public SimpleStringProperty statusProperty() {
        return status;
    }

    public SimpleStringProperty transactionIdProperty() {
        return transactionId;
    }

    public String getSenderPublicKey() {
        return senderPublicKey.get();
    }

    public SimpleStringProperty senderPublicKeyProperty() {
        return senderPublicKey;
    }

    public String getReceiverPublicKey() {
        return receiverPublicKey.get();
    }

    public SimpleStringProperty receiverPublicKeyProperty() {
        return receiverPublicKey;
    }
}
