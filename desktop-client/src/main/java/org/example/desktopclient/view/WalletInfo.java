package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.util.Duration;
import org.example.desktopclient.controller.UTXOController;
import org.example.desktopclient.controller.WalletController;
import org.example.desktopclient.model.TableTransactionInfo;

import java.util.Objects;

public class WalletInfo extends VBox {
    private final WalletController walletController = WalletController.getInstance();
    private final UTXOController utxoController = UTXOController.getInstance();
    private Image coinIcon = new Image(Objects.requireNonNull(getClass().getResource("/org/example/desktopclient/images/coin_icon.png")).toExternalForm());

    public WalletInfo(StackPane root, String walletPublicKey) {
        utxoController.updateWalletBalanceProperty(walletPublicKey);
        utxoController.updateTotalBalanceProperty();
        walletController.updateWalletTransactions(walletPublicKey);

        getStyleClass().addAll("wallet-info");
        /*HBox.setMargin(this, new Insets(0, 0, 0, 20));*/

        Label title = new Label();
        title.textProperty().bind(walletController.getWalletsModel().getWalletNameProperty(walletPublicKey));

        title.getStyleClass().addAll("wallet-title");

        VBox firstSection = new VBox();
        firstSection.setAlignment(Pos.CENTER_LEFT);
        firstSection.getStyleClass().addAll("section");
        firstSection.setSpacing(24);

        /*firstSection.setAlignment(Pos.TOP_CENTER);*/
        firstSection.prefWidthProperty().bind(this.widthProperty().multiply(.9));
        firstSection.maxWidthProperty().bind(this.widthProperty().multiply(.9));
        firstSection.prefHeightProperty().bind(this.heightProperty().multiply(.28));
        HBox.setMargin(firstSection, new Insets(15, 0, 0, 0));

        // Wallet addr

        TextField walletAddress = new TextField(walletPublicKey);
        walletAddress.setEditable(false);
        walletAddress.getStyleClass().addAll("wallet-address");
        walletAddress.prefWidthProperty().bind(firstSection.widthProperty().multiply(.8));
        walletAddress.prefHeightProperty().bind(firstSection.heightProperty().multiply(.19));

        Label walletAddressLabel = new Label("Address:");
        walletAddressLabel.getStyleClass().addAll("wallet-address-label");

        HBox walletAddressWrapper = new HBox(walletAddress);
        VBox walletAddressBox = new VBox(walletAddressLabel, walletAddressWrapper);

        // Wallet Stats

        HBox walletBalanceBox = new HBox();
        walletBalanceBox.setSpacing(150);

        VBox walletBalanceContent = new VBox();
        Label walletBalanceLabel = new Label("Wallet balance:");
        walletBalanceLabel.getStyleClass().addAll("wallet-balance-label");
        Label walletBalanceValue = new Label("0.00 coins");
        walletBalanceValue.textProperty().bind(utxoController.getWalletBalanceProperty(walletPublicKey));
        walletBalanceValue.getStyleClass().addAll("wallet-balance-value");

        ImageView walletBalanceCoinIconView = new ImageView(coinIcon);
        walletBalanceCoinIconView.setFitWidth(24);
        walletBalanceCoinIconView.setFitHeight(24);

        HBox iconBalanceBox = new HBox(walletBalanceCoinIconView, walletBalanceValue);
        iconBalanceBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(walletBalanceContent, new Insets(0, 0, 0, 8));
        walletBalanceContent.getChildren().addAll(walletBalanceLabel, iconBalanceBox);

        VBox totalBalanceContent = new VBox();
        Label totalBalanceLabel = new Label("Total balance:");
        totalBalanceLabel.getStyleClass().addAll("wallet-balance-label");

        Label totalBalanceValue = new Label("0.00 coins");
        totalBalanceValue.textProperty().bind(utxoController.getTotalBalanceProperty());
        totalBalanceValue.getStyleClass().addAll("wallet-balance-value");

        ImageView totalBalanceCoinIconView = new ImageView(coinIcon);
        totalBalanceCoinIconView.setFitWidth(24);
        totalBalanceCoinIconView.setFitHeight(24);

        HBox totalBalanceBox = new HBox(totalBalanceCoinIconView, totalBalanceValue);
        totalBalanceBox.setAlignment(Pos.CENTER_LEFT);
        totalBalanceContent.getChildren().addAll(totalBalanceLabel, totalBalanceBox);

        // Send button

        ImageView coinIconView = new ImageView(coinIcon);
        coinIconView.setFitWidth(24);
        coinIconView.setFitHeight(24);

        Button sendButton = new Button("Send", coinIconView);
        sendButton.getStyleClass().addAll("send-button");
        sendButton.setOnMouseClicked(event -> {
            CreateTransactionModal createTransactionModal = new CreateTransactionModal(root, walletPublicKey);
            createTransactionModal.prefWidthProperty().bind(root.widthProperty());
            createTransactionModal.prefHeightProperty().bind(root.heightProperty());

            BoxBlur blur = new BoxBlur(3, 3, 2);
            for(Node node : root.getChildren()) {
                node.setEffect(blur);
            }

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), createTransactionModal);
            createTransactionModal.setOpacity(0);
            fadeIn.setToValue(1);

            StackPane.setAlignment(createTransactionModal, Pos.CENTER);
            root.getChildren().addAll(createTransactionModal);
            fadeIn.play();

            setOnKeyPressed(e -> {
                if (e.getCode().toString().equals("ESCAPE")) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), createTransactionModal);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(ev -> {
                        root.getChildren().removeAll(createTransactionModal);
                        for(Node node : root.getChildren()) {
                            node.setEffect(null);
                        }
                    });
                    fadeOut.play();
                }
            });
        });

        walletBalanceBox.setAlignment(Pos.CENTER_LEFT);
        walletBalanceBox.getChildren().addAll(walletBalanceContent, totalBalanceContent, sendButton);

        // Second section

        VBox secondSection = new VBox();
        secondSection.getStyleClass().addAll("section");
        secondSection.prefWidthProperty().bind(this.widthProperty().multiply(.9));
        secondSection.prefHeightProperty().bind(this.heightProperty().multiply(.536));

        // Section title
        Label secondSectionTitle = new Label("Transactions");
        secondSectionTitle.getStyleClass().addAll("section-title");

        // Transaction Table
        ObservableList<TableTransactionInfo> transactions = FXCollections.observableArrayList();
        transactions.addAll(walletController.getWalletTransactions());

        TableView<TableTransactionInfo> transactionTable = new TableView<>(transactions);
        Label noTransactionsLabel = new Label("No transactions found");
        noTransactionsLabel.getStyleClass().add("no-transactions-label");
        transactionTable.setPlaceholder(noTransactionsLabel);

        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        transactionTable.prefWidthProperty().bind(secondSection.widthProperty().multiply(.96));
        transactionTable.getStyleClass().addAll("transaction-table");

        transactionTable.setRowFactory(t -> {
            TableRow<TableTransactionInfo> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    TableTransactionInfo transactionSelected = row.getItem();
                    if (transactionSelected != null) {
                        TransactionModal transactionModal = new TransactionModal(root, transactionSelected.getTransactionId());
                        transactionModal.prefWidthProperty().bind(root.widthProperty());
                        transactionModal.prefHeightProperty().bind(root.heightProperty());

                        BoxBlur blur = new BoxBlur(3, 3, 2);
                        for(Node node : root.getChildren()) {
                            node.setEffect(blur);
                        }

                        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), transactionModal);
                        transactionModal.setOpacity(0);
                        fadeIn.setToValue(1);

                        StackPane.setAlignment(transactionModal, Pos.CENTER);
                        root.getChildren().addAll(transactionModal);
                        fadeIn.play();

                        setOnKeyPressed(e -> {
                            if (e.getCode().toString().equals("ESCAPE")) {
                                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), transactionModal);
                                fadeOut.setToValue(0);
                                fadeOut.setOnFinished(ev -> {
                                    root.getChildren().removeAll(transactionModal);
                                    for(Node node : root.getChildren()) {
                                        node.setEffect(null);
                                    }
                                });
                                fadeOut.play();
                            }
                        });

                    }
                }
            });
            return row;
        });

        TableColumn<TableTransactionInfo, Long> dateColumn = new TableColumn<>("Date");
        dateColumn.getStyleClass().addAll("transaction-table-column");
        dateColumn.setCellValueFactory(new PropertyValueFactory<TableTransactionInfo, Long>("timeStamp"));

        TableColumn<TableTransactionInfo, Long> amountColumn = new TableColumn<>("Amount");
        amountColumn.getStyleClass().addAll("transaction-table-column");
        amountColumn.setCellValueFactory(new PropertyValueFactory<TableTransactionInfo, Long>("amount"));

        TableColumn<TableTransactionInfo, String> statusColumn = new TableColumn<>("Status");
        statusColumn.getStyleClass().addAll("transaction-table-column");
        statusColumn.setCellValueFactory(new PropertyValueFactory<TableTransactionInfo, String>("status"));

        TableColumn<TableTransactionInfo, String> hashColumn = new TableColumn<>("Transaction Id");
        hashColumn.getStyleClass().addAll("transaction-table-column");
        hashColumn.setCellValueFactory(new PropertyValueFactory<TableTransactionInfo, String>("transactionId"));

        transactionTable.getColumns().addAll(dateColumn, amountColumn, statusColumn, hashColumn);

        HBox transactionTableWrapper = new HBox(transactionTable);
        transactionTableWrapper.setAlignment(Pos.CENTER);
        secondSection.getChildren().addAll(secondSectionTitle, transactionTableWrapper);
        secondSection.setSpacing(7);

        // Add components to the first section

        VBox.setMargin(walletAddressBox, new Insets(0, 0, 5, 20));
        VBox.setMargin(walletBalanceBox, new Insets(0, 0, 5, 20));
        firstSection.getChildren().addAll(walletAddressBox, walletBalanceBox);
        HBox firstSectionWrapper = new HBox(firstSection);
        firstSectionWrapper.setAlignment(Pos.CENTER);
        HBox secondSectionWrapper = new HBox(secondSection);
        secondSectionWrapper.setAlignment(Pos.CENTER);
        VBox.setMargin(secondSectionWrapper, new Insets(30, 0, 0, 0));

        getChildren().addAll(title, firstSectionWrapper, secondSectionWrapper);
    }
}
