package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.example.desktopclient.controller.BlockchainController;
import org.example.desktopclient.controller.TransactionController;
import org.example.desktopclient.model.Block;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BlockModal extends StackPane {
    private final TransactionController transactionController = TransactionController.getInstance();
    private final BlockchainController blockchainController = BlockchainController.getInstance();

    public BlockModal(StackPane root, Block block){
        VBox background = new VBox();
        background.prefWidthProperty().bind(prefWidthProperty());
        background.getStyleClass().addAll("transaction-modal");
        //setAlignment(Pos.CENTER);

        // Content

        VBox content = new VBox();
        content.prefWidthProperty().bind(background.prefWidthProperty().multiply(.3));
        content.getStyleClass().addAll("transaction-content");

        Label title = new Label("Block #" + block.getIndex());
        title.getStyleClass().addAll("transaction-title");
        //VBox.setMargin(title, new Insets(0, 0, 0, 0));

        VBox firstSection = new VBox();
        firstSection.setSpacing(1);
        firstSection.getStyleClass().addAll("transaction-section");

        Text hashLabel = new Text("Hash: " + block.getBlockHash());
        hashLabel.getStyleClass().addAll("transaction-label");

        Text prevHashLabel = new Text("Previous Hash: " + block.getPreviousHash());
        prevHashLabel.getStyleClass().addAll("transaction-label");

        Text nonceLabel = new Text("Nonce: " + block.getNonce());
        prevHashLabel.getStyleClass().addAll("transaction-label");

        Text sizeBytesLabel = new Text("Size: " + blockchainController.getFormattedSize(block.getSizeInBytes()));

        Label timestampLabel = new Label("Date: " + Instant.ofEpochMilli(block.getTimeStamp()).atZone(ZoneId.systemDefault()).format(transactionController.formatter));
        timestampLabel.getStyleClass().addAll("transaction-label");

        firstSection.getChildren().addAll(hashLabel, prevHashLabel, nonceLabel, sizeBytesLabel, timestampLabel);

        VBox secondSection = new VBox();
        secondSection.setSpacing(5);
        secondSection.getStyleClass().addAll("transaction-section");

        Label transactionLabel = new Label("Transactions:");
        transactionLabel.getStyleClass().addAll("transaction-label");

        ObservableList<Pair<String, Long>> transactions = FXCollections.observableArrayList(block.getTransactions()
                .stream()
                .map(e -> new Pair<>(e.getTransactionId(), e.getAmount()))
                .collect(Collectors.toList()));
        TableView<Pair<String, Long>> transactionTable = new TableView<>(transactions);
        transactionTable.prefHeightProperty().bind(prefHeightProperty().multiply(.9));
        transactionTable.getStyleClass().addAll("modal-transaction-table");
        Label noTransactionsLabel = new Label("No transactions found");
        noTransactionsLabel.getStyleClass().addAll("no-transactions-placeholder");
        transactionTable.setPlaceholder(noTransactionsLabel);
        transactionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Pair<String, Long>, String> txIdColumn = new TableColumn<>("Transaction ID");
        txIdColumn.minWidthProperty().bind(transactionTable.widthProperty().multiply(.68));
        txIdColumn.setCellFactory(col -> {
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
        txIdColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getKey()));

        TableColumn<Pair<String, Long>, String> txAmountColumn = new TableColumn<>("Amount");
        txAmountColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getValue() / 100_000_000.0 + " coins"));

        transactionTable.getColumns().addAll(txIdColumn, txAmountColumn);

        secondSection.getChildren().addAll(transactionLabel, transactionTable);

        content.getChildren().addAll(title, firstSection, secondSection);

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
