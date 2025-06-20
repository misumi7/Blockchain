package org.example.desktopclient.controller;

import org.example.desktopclient.model.TransactionsModel;

public class TransactionController {
    private TransactionsModel transactionsModel = TransactionsModel.getInstance();
    private static TransactionController instance;

    private TransactionController() {}

    public static TransactionController getInstance() {
        if (instance == null) {
            instance = new TransactionController();
        }
        return instance;
    }


}
