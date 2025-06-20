package org.example.desktopclient.model;

import java.util.ArrayList;
import java.util.List;

public class TransactionsModel {
    private static TransactionsModel instance;

    private TransactionsModel() {}

    public static TransactionsModel getInstance() {
        if (instance == null) {
            instance = new TransactionsModel();
        }
        return instance;
    }
}
