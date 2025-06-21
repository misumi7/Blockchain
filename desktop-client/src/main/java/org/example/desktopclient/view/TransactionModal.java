package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.example.desktopclient.controller.TransactionController;
import org.example.desktopclient.model.Transaction;
import org.example.desktopclient.model.TransactionStatus;
import org.example.desktopclient.model.UTXO;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;

public class TransactionModal extends StackPane {
    private TransactionController transactionController = TransactionController.getInstance();
    public TransactionModal(StackPane root, String transactionId) {
        getStyleClass().addAll("modal-container");
        transactionController.updateTransactionDetails(transactionId);

        VBox background = new VBox();
        background.prefWidthProperty().bind(prefWidthProperty());
        background.getStyleClass().addAll("transaction-modal");
        //setAlignment(Pos.CENTER);

        // Content

        VBox content = new VBox();
        content.prefWidthProperty().bind(background.prefWidthProperty().multiply(.5));
        content.getStyleClass().addAll("transaction-content");

        Label title = new Label("Transaction Details:");
        title.getStyleClass().addAll("transaction-title");
        VBox.setMargin(title, new Insets(0, 0, 15, 0));

        // First Section

        VBox firstSection = new VBox();
        firstSection.getStyleClass().addAll("first-transaction-section");

        Label hashLabel = new Label("Hash: " + transactionController.getDisplayedTransaction().getTransactionId());
        hashLabel.setWrapText(true);
        hashLabel.getStyleClass().addAll("transaction-section-label");

        Label statusLabel = new Label("Status: ");
        Label statusValue = new Label(transactionController.getDisplayedTransaction().getStatus().toString());
        HBox statusBox = new HBox(statusLabel, statusValue);
        statusBox.setAlignment(Pos.CENTER_LEFT);
        statusLabel.getStyleClass().addAll("transaction-section-label");
        statusValue.setStyle("-fx-background-color: " + (transactionController.getDisplayedTransaction().getStatus() == TransactionStatus.CONFIRMED ? "#4CAF50" : transactionController.getDisplayedTransaction().getStatus() == TransactionStatus.PENDING ? "#FF9800" : "#F44336") + ";");
        statusValue.getStyleClass().addAll("transaction-status-value");

        Label dateLabel = new Label("Date: " + Instant.ofEpochMilli(transactionController.getDisplayedTransaction().getTimeStamp()).atZone(ZoneId.systemDefault()).format(transactionController.formatter));

        firstSection.getChildren().addAll(hashLabel, statusBox, dateLabel);

        // Second section

        VBox secondSection = new VBox();
        secondSection.getStyleClass().addAll("transaction-section");

        Label amountLabel = new Label("Amount: " + transactionController.getDisplayedTransaction().getAmount() / 100_000_000.0 + " coins");
        amountLabel.getStyleClass().addAll("transaction-section-label");

        Text senderLabel = new Text("Sender: " + (!transactionController.getDisplayedTransaction().getSenderPublicKey().isEmpty() ? transactionController.getDisplayedTransaction().getSenderPublicKey() : "Unknown"));
        senderLabel.getStyleClass().addAll("transaction-section-label");

        Text receiverLabel = new Text("Receiver: " + transactionController.getDisplayedTransaction().getReceiverPublicKey());
        /*receiverLabel.setWrapText(true);
        receiverLabel.minWidthProperty().bind(content.widthProperty().multiply(.8));
        receiverLabel.maxHeightProperty().bind(content.heightProperty().multiply(.5));*/
        receiverLabel.getStyleClass().addAll("transaction-section-label");

        secondSection.getChildren().addAll(amountLabel, senderLabel, receiverLabel);

        // Third section

        VBox thirdSection = new VBox();
        thirdSection.setSpacing(2);
        thirdSection.getStyleClass().addAll("transaction-section");

        Label inputsLabel = new Label("Inputs:");
        ObservableList<String> inputs = FXCollections.observableArrayList(transactionController.getDisplayedTransaction().getInputs());
        TableView<String> inputsTable = new TableView<>();
        inputsTable.getStyleClass().addAll("modal-transaction-table");
        inputsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        inputsTable.setItems(inputs);

        Label inputsPlaceholder = new Label("No inputs");
        inputsPlaceholder.getStyleClass().addAll("transaction-placeholder");
        inputsTable.setPlaceholder(inputsPlaceholder);

        TableColumn<String, String> trIdColumn = new TableColumn<>("Transaction Id");
        trIdColumn.minWidthProperty().bind(inputsTable.widthProperty().multiply(.38));
        trIdColumn.setCellFactory(col -> {
            return new TableCell<>() {
                private final Text text = new Text();

                {
                    text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));
                    setGraphic(text);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                    }
                    else {
                        text.setText(item);
                    }
                }
            };
        });
        trIdColumn.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split(":");
            return new SimpleStringProperty(parts.length > 2 ? parts[1].trim() : "Undefined");
        });

        TableColumn<String, String> outputIndexColumn = new TableColumn<>("Output Index");
        outputIndexColumn.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split(":");
            return new SimpleStringProperty(parts.length > 2 ? parts[2].trim() : "Undefined");
        });

        TableColumn<String, String> ownerColumn = new TableColumn<>("Owner");
        ownerColumn.minWidthProperty().bind(inputsTable.widthProperty().multiply(.38));
        ownerColumn.setCellFactory(col -> {
            return new TableCell<>() {
                private final Text text = new Text();

                {
                    text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));
                    setGraphic(text);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                    }
                    else {
                        text.setText(item);
                    }
                }
            };
        });
        ownerColumn.setCellValueFactory(cellData -> {
            String[] parts = cellData.getValue().split(":");
            return new SimpleStringProperty(parts.length > 2 ? parts[0].trim() : "Undefined");
        });

        inputsTable.getColumns().addAll(trIdColumn, outputIndexColumn, ownerColumn);

        // Outputs Table

        Label outputsLabel = new Label("Outputs:");
        outputsLabel.getStyleClass().addAll("transaction-section-label");

        ObservableList<UTXO> outputs = FXCollections.observableArrayList(transactionController.getDisplayedTransaction().getOutputs());
        TableView<UTXO> outputsTable = new TableView<>(outputs);
        outputsTable.getStyleClass().addAll("modal-transaction-table");
        outputsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<UTXO, String> outputIxColumn = new TableColumn<>("Output Index");
        outputIxColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getOutputIndex())));

        TableColumn<UTXO, String> amountColumn = new TableColumn<>("Amount");
        amountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(String.valueOf(cellData.getValue().getAmount() / 100_000_000.0)));

        TableColumn<UTXO, String> secOwnerColumn = new TableColumn<>("Owner");
        secOwnerColumn.minWidthProperty().bind(outputsTable.widthProperty().multiply(.46));
        secOwnerColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOwner()));
        secOwnerColumn.setCellFactory(col -> {
            return new TableCell<>() {
                private final Text text = new Text();

                {
                    text.wrappingWidthProperty().bind(col.widthProperty().subtract(10));
                    setGraphic(text);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                    }
                    else {
                        text.setText(item);
                    }
                }
            };
        });

        outputsTable.getColumns().addAll(outputIxColumn, amountColumn, secOwnerColumn);

        // Fee & Signature labels

        Label feeLabel = new Label("Fee: " + String.format("%f", transactionController.getDisplayedTransaction().getTransactionFee() / 100_000_000.0) + " coins");
        feeLabel.getStyleClass().addAll("transaction-section-label");

        Text signatureLabel = new Text("Signature: " + Base64.getEncoder().encodeToString(transactionController.getDisplayedTransaction().getDigitalSignature()));
        signatureLabel.getStyleClass().addAll("transaction-section-label");
        /*signatureLabel.setWrapText(true);
        signatureLabel.minWidthProperty().bind(content.widthProperty().multiply(.86));
        signatureLabel.minHeightProperty().bind(content.heightProperty().multiply(.05));*/

        thirdSection.getChildren().addAll(inputsLabel, inputsTable, outputsLabel, outputsTable, feeLabel, signatureLabel);

        //

        content.getChildren().addAll(title, firstSection, secondSection, thirdSection);

        HBox contentWrapper = new HBox(content);
        content.maxHeightProperty().bind(prefHeightProperty().multiply(.95));
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
