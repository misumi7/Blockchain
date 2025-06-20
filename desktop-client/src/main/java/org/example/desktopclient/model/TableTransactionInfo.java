package org.example.desktopclient.model;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

public class TableTransactionInfo {
    private SimpleStringProperty timeStamp;
    private SimpleDoubleProperty amount;
    private SimpleStringProperty status;
    private SimpleStringProperty transactionId;

    public TableTransactionInfo(String timeStamp, double amount, TransactionStatus status, String transactionId) {
        this.timeStamp = new SimpleStringProperty(timeStamp);
        this.amount = new SimpleDoubleProperty(amount);
        this.status = new SimpleStringProperty(status.toString().substring(0, 1) + status.toString().substring(1).toLowerCase());
        this.transactionId = new SimpleStringProperty(transactionId);
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
}
