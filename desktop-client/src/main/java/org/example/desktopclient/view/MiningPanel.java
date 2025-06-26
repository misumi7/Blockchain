package org.example.desktopclient.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import javafx.util.StringConverter;
import org.example.desktopclient.controller.BlockchainController;
import org.example.desktopclient.controller.WalletController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

public class MiningPanel extends VBox {
    private static MiningPanel instance;
    private final WalletController walletsController = WalletController.getInstance();
    //private Map<String, String> walletNames;
    private Label mempoolSizeLabel;
    private Label blockDurationValue;
    private Label hashRateValue;
    private Label sessionRewardValue;
    //private ComboBox<Pair<String, SimpleStringProperty>> walletComboBox;
    private final BlockchainController blockchainController = BlockchainController.getInstance();
    private ComboBox<Pair<String, SimpleStringProperty>> walletComboBox;
    private boolean isMining = false;

    public MiningPanel() {
        instance = this;

        getStyleClass().addAll("node");
        InnerShadow innerShadow = new InnerShadow();
        innerShadow.setOffsetY(2);
        innerShadow.setColor(Color.rgb(0, 0, 0, 0.3));

        setEffect(innerShadow);
        //setAlignment(Pos.CENTER);
        /*setSpacing(10);*/
        setPadding(new Insets(10, 0, 0, 45));

        Label title = new Label("Mining");
        title.getStyleClass().addAll("mining-title");

        HBox miningOptions = new HBox();
        miningOptions.getStyleClass().addAll("wallet-selection-box");

        //ObservableList<String> wallets = FXCollections.observableArrayList();
        Map<String, SimpleStringProperty> walletNames = walletsController.getWalletsModel().getWalletNames();
        ObservableList<Pair<String, SimpleStringProperty>> walletList = FXCollections.observableArrayList();
        walletNames.forEach((k, v) -> {
            walletList.add(new Pair<>(k, v));
        });
        walletComboBox = new ComboBox<>();
        walletComboBox.setConverter(new StringConverter<Pair<String, SimpleStringProperty>>() {
            @Override
            public String toString(Pair<String, SimpleStringProperty> pair) {
                return pair == null ? "" : pair.getValue().getValue();
            }

            @Override
            public Pair<String, SimpleStringProperty> fromString(String string) {
                return null;
            }
        });

        walletComboBox.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Pair<String, SimpleStringProperty> item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getValue().getValue());
            }
        });

        walletComboBox.setItems(walletList);
        walletComboBox.setPromptText("Select a wallet");
        if(!walletList.isEmpty()) {
            walletComboBox.setValue(walletList.getFirst());
        }
        walletComboBox.getStyleClass().addAll("wallet-combo-box");
        walletComboBox.prefWidthProperty().bind(this.widthProperty().multiply(.57));
        walletComboBox.prefHeightProperty().bind(this.heightProperty().multiply(.05));

        ImageView arrowIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/show_more_icon.png"))));
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
        miningButton.getStyleClass().addAll("mining-button", "start-mining-button");

        HBox miningButtonBox = new HBox(miningButton);
        miningButtonBox.prefWidthProperty().bind(this.widthProperty().multiply(.2));
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
        logArea.prefHeightProperty().bind(this.heightProperty().multiply(.53));
        //logArea.setText("Mining logs...");

        miningButton.setOnAction(event -> {
            isMining = !isMining;

            if(isMining) {
                Pair<String, SimpleStringProperty> selectedWallet = walletComboBox.getValue();
                //System.out.println("Selected wallet: " + (selectedWallet != null ? selectedWallet.getKey() : "None"));
                if (selectedWallet == null || selectedWallet.getKey().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Please select a wallet to start mining.", ButtonType.OK);
                    alert.showAndWait();
                    return;
                }
                addLog(logArea, String.format("[%s] Starting mining with wallet: %s", LocalDateTime.now().format(BlockchainController.TIME_FORMATTER), selectedWallet.getKey().substring(0, selectedWallet.getKey().length() / 2) + "..."));
                blockchainController.startMining(this, selectedWallet.getKey(), (message) -> {
                    addLog(logArea, String.format("[%s] %s", LocalDateTime.now().format(BlockchainController.TIME_FORMATTER), message));
                });
            }
            else {
                addLog(logArea, String.format("[%s] Stopping mining...", LocalDateTime.now().format(BlockchainController.TIME_FORMATTER)));
                blockchainController.stopMining((message) -> {
                    addLog(logArea, String.format("[%s] %s", LocalDateTime.now().format(BlockchainController.TIME_FORMATTER), message));
                });
            }

            // change button style
            miningButton.setText(isMining ? "Stop" : "Start");
            miningButton.getStyleClass().add(isMining ? "stop-mining-button" : "start-mining-button");
            miningButton.getStyleClass().remove(isMining ? "start-mining-button" : "stop-mining-button");
        });

        HBox miningInfoBox = new HBox();
        miningInfoBox.getStyleClass().addAll("mining-info-box");
        miningInfoBox.prefWidthProperty().bind(this.widthProperty().multiply(.9));
        miningInfoBox.prefHeightProperty().bind(this.heightProperty().multiply(.12));
        miningInfoBox.setSpacing(53);
        VBox.setMargin(miningInfoBox, new Insets(40, 60, 0, 5));
        miningInfoBox.setAlignment(Pos.CENTER);

        // --

        VBox hashRateBox = new VBox();
        hashRateBox.getStyleClass().addAll("mining-info-component");
        hashRateBox.prefWidthProperty().bind(miningInfoBox.widthProperty().multiply(.23));
        hashRateBox.setAlignment(Pos.CENTER);

        Label hashRateLabel = new Label("Performance:");
        hashRateLabel.getStyleClass().addAll("mining-info-component-title");
        hashRateValue = new Label("0 H/s");
        hashRateValue.getStyleClass().addAll("mining-info-component-value");
        hashRateBox.getChildren().addAll(hashRateLabel, hashRateValue);

        // --

        VBox blockDurationBox = new VBox();
        blockDurationBox.getStyleClass().addAll("mining-info-component");
        blockDurationBox.prefWidthProperty().bind(miningInfoBox.widthProperty().multiply(.23));
        blockDurationBox.setAlignment(Pos.CENTER);

        Label blockDurationLabel = new Label("Previous block:");
        blockDurationLabel.getStyleClass().addAll("mining-info-component-title");
        blockDurationValue = new Label("0 s");
        blockDurationValue.getStyleClass().addAll("mining-info-component-value");
        blockDurationBox.getChildren().addAll(blockDurationLabel, blockDurationValue);

        // --

        VBox sessionRewardBox = new VBox();
        sessionRewardBox.getStyleClass().addAll("mining-info-component");
        sessionRewardBox.prefWidthProperty().bind(miningInfoBox.widthProperty().multiply(.23));
        sessionRewardBox.setAlignment(Pos.CENTER);

        Label sessionRewardLabel = new Label("Session reward:");
        sessionRewardLabel.getStyleClass().addAll("mining-info-component-title");
        sessionRewardValue = new Label("0.00 coins");
        sessionRewardValue.getStyleClass().addAll("mining-info-component-value");
        sessionRewardBox.getChildren().addAll(sessionRewardLabel, sessionRewardValue);

        miningInfoBox.getChildren().addAll(hashRateBox, blockDurationBox, sessionRewardBox);
        getChildren().addAll(title, miningOptions, logFieldWrapper, miningInfoBox);
    }

    public static MiningPanel getInstance() {
        return instance;
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

    public void updateComboBoxOptions(){
        Map<String, SimpleStringProperty> walletNames = walletsController.getWalletsModel().getWalletNames();
        ObservableList<Pair<String, SimpleStringProperty>> walletList = FXCollections.observableArrayList();
        walletNames.forEach((k, v) -> {
            walletList.add(new Pair<>(k, v));
        });
        walletComboBox.setItems(walletList);
        if (!walletList.isEmpty()) {
            walletComboBox.setValue(walletList.getFirst());
        } else {
            walletComboBox.setValue(null);
        }
    }

    public void updatePerformance(long performance) {
        Platform.runLater(() -> {
            if(performance < 1_000){
                hashRateValue.setText(performance + " H/s");
            }
            else if(performance < 1_000_000){
                hashRateValue.setText(String.format("%d KH/s", performance / 1_000));
            }
            else if(performance < 1_000_000_000){
                hashRateValue.setText(String.format("%d MH/s", performance / 1_000_000));
            }
            else {
                hashRateValue.setText(String.format("%d GH/s", performance / 1_000_000_000));
            }
        });
    }

    public void updateBlockMiningDuration(long duration) {
        Platform.runLater(() -> {
            if (duration < 1000) {
                blockDurationValue.setText(duration + " ms");
            } else if (duration < 60_000) {
                blockDurationValue.setText(String.format("%.2f s", duration / 1000.0));
            } else {
                long mins = duration / 60_000;
                long s = (duration % 60_000) / 1000;
                blockDurationValue.setText(String.format("%d min %d s", mins, s));
            }
        });
    }

    public void updateSessionReward(long sessionReward) {
        Platform.runLater(() -> {
            sessionRewardValue.setText(String.format("%.2f coins", sessionReward / 100_000_000.0));
        });
    }
}
