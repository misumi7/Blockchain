package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.example.desktopclient.controller.TransactionController;
import org.example.desktopclient.controller.WalletController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CreateTransactionModal extends StackPane {
    private TransactionController transactionController = TransactionController.getInstance();

    public CreateTransactionModal(StackPane root, WalletInfo parent, String walletPublicKey, String currentTransactionFilterValue) {

        VBox background = new VBox();
        background.prefWidthProperty().bind(prefWidthProperty());
        background.getStyleClass().addAll("transaction-modal");
        //setAlignment(Pos.CENTER);

        // Content

        VBox content = new VBox();
        content.prefWidthProperty().bind(background.prefWidthProperty().multiply(.3));
        content.getStyleClass().addAll("transaction-content");

        Label title = new Label("New Transaction:");
        title.getStyleClass().addAll("transaction-title");
        VBox.setMargin(title, new Insets(0, 0, 15, 0));

        Label sender = new Label("Sender:");
        sender.getStyleClass().addAll("create-transaction-label");
        TextField senderField = new TextField();
        senderField.setText(walletPublicKey);
        senderField.setEditable(false);
        senderField.getStyleClass().addAll("create-transaction-field");

        Label receiver = new Label("Receiver:");
        VBox.setMargin(receiver, new Insets(8, 0, 0, 0));
        receiver.getStyleClass().addAll("create-transaction-label");
        TextField receiverField = new TextField();
        receiverField.getStyleClass().addAll("create-transaction-field");

        Label amount = new Label("Amount:");
        VBox.setMargin(amount, new Insets(8, 0, 0, 0));
        amount.getStyleClass().addAll("create-transaction-label");
        TextField amountField = new TextField();
        amountField.setTextFormatter(new TextFormatter<>(change ->
                change.getControlNewText().matches("\\d*(\\.\\d*)?") ? change : null));
        amountField.getStyleClass().addAll("create-transaction-field");

        // PIN field

        Label pinLabel = new Label("PIN Code:");
        HBox pinLabelBox = new HBox(pinLabel);
        pinLabelBox.setAlignment(Pos.CENTER);
        pinLabel.getStyleClass().addAll("create-transaction-label");
        VBox.setMargin(pinLabelBox, new Insets(10, 0, 5, 0));

        HBox pinBox = new HBox();
        pinBox.setSpacing(9);
        pinBox.setAlignment(Pos.CENTER);
        List<TextField> pinFields = new ArrayList<>();
        for(int i = 0; i < 6; ++i){
            TextField pinField = new TextField();
            pinField.prefWidthProperty().bind(content.widthProperty().multiply(.1));
            pinField.minHeightProperty().bind(content.heightProperty().multiply(.109));
            pinField.maxHeightProperty().bind(content.heightProperty().multiply(.109));
            pinField.getStyleClass().addAll("create-transaction-pin-field");

            pinField.setPromptText("*");
            pinField.setTextFormatter(new TextFormatter<>(change ->
                    change.getControlNewText().matches("\\d{0,1}") ? change : null));

            int ind = i;
            pinField.textProperty().addListener((obs, oldValue, newValue) -> {
                if(!newValue.isEmpty() && ind < 5) {
                    pinFields.get(ind + 1).requestFocus();
                }
            });

            pinField.setOnKeyPressed(e -> {
                if(e.getCode() == KeyCode.BACK_SPACE && pinField.getText().isEmpty() && ind > 0) {
                    pinFields.get(ind - 1).requestFocus();
                }
            });

            pinFields.add(pinField);
            pinBox.getChildren().add(pinField);
        }

        // Send Button

        Image sendIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/send_transaction_icon.png")));
        ImageView sendIconView = new ImageView(sendIcon);
        sendIconView.setFitWidth(23);
        sendIconView.setFitHeight(23);
        sendIconView.setPreserveRatio(true);
        Button sendButton = new Button("Send", sendIconView);
        sendButton.getStyleClass().addAll("create-transaction-button");
        sendButton.setOnMouseClicked(e -> {
            String receiverPublicKey = receiverField.getText().trim();
            String amountValue = amountField.getText().trim();

            StringBuilder pinCode = new StringBuilder();
            for(TextField pinField : pinFields) {
                pinCode.append(pinField.getText());
            }
            String pin = pinCode.toString();

            if(transactionController.createTransaction(walletPublicKey, receiverPublicKey, Double.parseDouble(amountValue), pin)){
                WalletController.getInstance().updateWalletTransactions(walletPublicKey, currentTransactionFilterValue);
                parent.updateTransactionList();
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    root.getChildren().removeAll(this);
                    for(Node node : root.getChildren()) {
                        node.setEffect(null);
                    }
                });
                fadeOut.play();
            }
        });

        HBox sendButtonBox = new HBox(sendButton);
        sendButtonBox.setAlignment(Pos.CENTER);
        VBox.setMargin(sendButtonBox, new Insets(17, 0, 0, 0));


        content.getChildren().addAll(title, sender, senderField, receiver, receiverField, amount, amountField, pinLabelBox, pinBox, sendButtonBox);

        HBox contentWrapper = new HBox(content);
        content.maxHeightProperty().bind(prefHeightProperty().multiply(.535));
        contentWrapper.setAlignment(Pos.CENTER);

        contentWrapper.setOnMouseClicked(e -> {
            if(e.getTarget() == contentWrapper) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    root.getChildren().removeAll(this);
                    for(Node node : root.getChildren()) {
                        node.setEffect(null);
                    }
                });
                fadeOut.play();
            }
        });

        getChildren().addAll(background, contentWrapper);
    }
}
