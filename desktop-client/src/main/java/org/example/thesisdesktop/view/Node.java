package org.example.thesisdesktop.view;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.example.thesisdesktop.controller.BlockchainController;

import java.net.http.HttpClient;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class Node extends VBox {
    private Map<String, String> walletNames;
    private Label mempoolSizeLabel;
    private ComboBox<Pair<String, String>> walletComboBox;
    private final BlockchainController blockchainController = BlockchainController.getInstance();

    public Node() {
        getStyleClass().addAll("node");
        //setAlignment(Pos.CENTER);
        /*setSpacing(10);*/
        setPadding(new Insets(10, 0, 0, 45));

        Label title = new Label("Mining");
        title.getStyleClass().addAll("mining-title");

        HBox miningOptions = new HBox();
        miningOptions.getStyleClass().addAll("wallet-selection-box");

        //ObservableList<String> wallets = FXCollections.observableArrayList();
        walletComboBox = new ComboBox();
        walletComboBox.getStyleClass().addAll("wallet-combo-box");
        walletComboBox.prefWidthProperty().bind(this.widthProperty().multiply(.57));
        walletComboBox.prefHeightProperty().bind(this.heightProperty().multiply(.05));

        ImageView arrowIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/thesisdesktop/images/show_more_icon.png"))));
        arrowIcon.setFitWidth(25);
        arrowIcon.setFitHeight(25);
        arrowIcon.setMouseTransparent(true);

        StackPane comboBoxContainer = new StackPane(walletComboBox, arrowIcon);
        StackPane.setAlignment(arrowIcon, Pos.CENTER_RIGHT);
        StackPane.setMargin(arrowIcon, new Insets(0, 8, 0, 0));

        mempoolSizeLabel = new Label("Mempool: 0 transactions");
        mempoolSizeLabel.getStyleClass().addAll("mempool-size");

        VBox walletSelectionBox = new VBox(comboBoxContainer, mempoolSizeLabel);

        Button miningButton = new Button("Start");
        //miningOptions.setAlignment(Pos.CENTER_LEFT);
        miningButton.getStyleClass().addAll("mining-button");

        HBox miningButtonBox = new HBox(miningButton);
        miningButtonBox.prefWidthProperty().bind(this.widthProperty().multiply(.14));
        miningButtonBox.setAlignment(Pos.CENTER);

        miningOptions.getChildren().addAll(walletSelectionBox, miningButtonBox);

        TextArea logArea = new TextArea();
        logArea.setWrapText(true);
        logArea.setScrollTop(Double.MAX_VALUE);
        Pane logFieldWrapper = new Pane(logArea);
        logArea.getStyleClass().addAll("log-area");
        logArea.setEditable(false);
        VBox.setMargin(logFieldWrapper, new Insets(40, 0, 0, 0));
        logArea.prefWidthProperty().bind(this.widthProperty().multiply(.9));
        logArea.prefHeightProperty().bind(this.heightProperty().multiply(.45));
        //logArea.setText("Mining logs...");

        miningButton.setOnAction(event -> {
            Pair<String, String> selectedWallet = walletComboBox.getValue();
            //System.out.println("Selected wallet: " + (selectedWallet != null ? selectedWallet.getKey() : "None"));
            if (selectedWallet == null || selectedWallet.getKey().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a wallet to start mining.", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            addLog(logArea, String.format("[%s] Starting mining with wallet: %s\n", LocalDateTime.now().format(BlockchainController.TIME_FORMATTER), selectedWallet.getKey().substring(0, selectedWallet.getKey().length() / 2) + "..."));
            blockchainController.startMining(selectedWallet.getKey(), (message) -> {
                addLog(logArea, String.format("[%s] %s\n", LocalDateTime.now().format(BlockchainController.TIME_FORMATTER), message));
            });
        });

        getChildren().addAll(title, miningOptions, logFieldWrapper);
    }

    private void addLog(TextArea logArea, String message) {
        Platform.runLater(() -> {
            logArea.appendText(message + "\n");
            logArea.setScrollTop(Double.MAX_VALUE);
        });
    }

    public void setMempoolTransactionCount(String mempoolSize) {
        this.mempoolSizeLabel.setText("Mempool: " + mempoolSize + " transactions");
    }

    public void setWalletNames(Map<String, String> walletNames) {
        /*if(walletNames == null || walletNames.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "No wallets available. Please create a wallet first.", ButtonType.OK);
            alert.showAndWait();
            return;
        }*/
        this.walletNames = walletNames;
        ObservableList<Pair<String, String>> walletList = FXCollections.observableArrayList();
        walletNames.forEach((k, v) -> {
            walletList.add(new Pair<>(k, v));
        });

        walletComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Pair<String, String> pair) {
                return pair == null ? "" : pair.getValue();
            }

            @Override
            public Pair<String, String> fromString(String string) {
                return null;
            }
        });

        walletComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<String, String> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getValue());
            }
        });

        walletComboBox.setItems(walletList);
        walletComboBox.setValue(walletList.getFirst());
    }
}
