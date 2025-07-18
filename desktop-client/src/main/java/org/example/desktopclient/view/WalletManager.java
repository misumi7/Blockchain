package org.example.desktopclient.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Pair;
import org.example.desktopclient.controller.UTXOController;
import org.example.desktopclient.controller.WalletController;
import org.example.desktopclient.model.TableWalletInfo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import java.util.stream.Collectors;

public class WalletManager extends VBox {
    public static WalletManager instance;
    private TableView<TableWalletInfo> walletTable  = new TableView<>();
    private final WalletController walletController = WalletController.getInstance();
    private final UTXOController utxoController = UTXOController.getInstance();
    private ObservableList<TableWalletInfo> wallets;

    public WalletManager(Stage stage) {
        instance = this;

        utxoController.updateWalletBalances();

        getStyleClass().addAll("wallet-manager");
        setSpacing(18);

        Label title = new Label("Wallet Manager");
        title.getStyleClass().addAll("wallet-manager-title");

        // Table

        /*ObservableList<Pair<String, SimpleStringProperty>> wallets = FXCollections.observableArrayList(walletController.getWalletNames()
                .entrySet()
                .stream()
                .map((e) -> new Pair<>(e.getKey(), e.getValue()))
                .collect(Collectors.toList()));*/

        wallets = FXCollections.observableArrayList(
                walletController.getWalletsModel().getWalletNames()
                        .entrySet()
                        .stream()
                        .map(e -> new TableWalletInfo(e.getKey(), e.getValue().get(), utxoController.getWalletBalanceProperty(e.getKey()).get()))
                        .collect(Collectors.toList())
        );

        walletTable.setItems(wallets);
        walletTable.setEditable(false);
        walletTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        walletTable.getStyleClass().addAll("wallet-table-view");
        Label placeholderLabel = new Label("No wallets found");
        placeholderLabel.getStyleClass().addAll("wallet-table-placeholder");
        walletTable.setPlaceholder(placeholderLabel);

        // Doesn't work for the first row for some reason
        walletTable.setRowFactory(tableView -> new TableRow<>() {
            @Override
            protected void updateItem(TableWalletInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    getStyleClass().remove("nempty-wallet-table-row");
                }
                else if(!getStyleClass().contains("nempty-wallet-table-row")){
                    getStyleClass().add("nempty-wallet-table-row");
                }
            }
        });

        TableColumn<TableWalletInfo, String> walletBalanceColumn = new TableColumn<>("Balance");
        walletBalanceColumn.setCellValueFactory(cellData -> cellData.getValue().getWalletBalanceProperty());
        walletBalanceColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                }
            }
        });

        TableColumn<TableWalletInfo, String> walletNameColumn = new TableColumn<>("Wallet Name");
        walletNameColumn.setCellValueFactory(cellData -> cellData.getValue().getWalletNameProperty());
        walletNameColumn.setCellFactory(column -> new TableCell<>() {
            private final TextField textField = new TextField();

            {
                setAlignment(Pos.CENTER);
                textField.setAlignment(Pos.CENTER);
                textField.setOnAction(e -> {
                    commitEdit(textField.getText());
                });

                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit(textField.getText());
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setContextMenu(null);
                }
                else {
                    if (isEditing()) {
                        textField.setText(item);
                        setText(null);
                        setGraphic(textField);
                    }
                    else {
                        setText(item);
                        setGraphic(null);
                    }
                }
            }

            @Override
            public void startEdit() {
                super.startEdit();
                if (getItem() == null)
                    return;
                textField.setText(getItem());
                setText(null);
                setGraphic(textField);
                textField.requestFocus();
                //textField.selectAll();
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getItem());
                setGraphic(null);
            }

            public void commitEdit(String newValue) {
                super.commitEdit(newValue);
                TableWalletInfo tableWalletInfo = getTableView().getItems().get(getIndex());
                walletController.setWalletName(tableWalletInfo.getPublicKey(), newValue);
                tableWalletInfo.getWalletNameProperty().set(newValue);
                setText(newValue);
                setGraphic(null);
                this.requestFocus();
            }

        });
        walletNameColumn.setOnEditCommit(event -> {
            TableWalletInfo tableWalletInfo = event.getRowValue();
            String newName = event.getNewValue();
            /*pair.getValue().set(newName);*/
            walletController.setWalletName(tableWalletInfo.getPublicKey(), newName);
            walletTable.setEditable(false);
        });

        TableColumn<TableWalletInfo, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button();
            private final Button deleteButton = new Button();
            private final Button downloadButton = new Button();
            private final HBox buttonsBox = new HBox(editButton, deleteButton, downloadButton);

            {
                buttonsBox.setAlignment(Pos.CENTER);

                Image editIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/edit_icon.png")));
                ImageView editIconView = new ImageView(editIcon);
                editIconView.setFitHeight(25);
                editIconView.setFitWidth(25);
                editButton.setGraphic(editIconView);

                Image deleteIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/delete_icon.png")));
                ImageView deleteIconView = new ImageView(deleteIcon);
                deleteIconView.setFitHeight(26);
                deleteIconView.setFitWidth(26);
                deleteButton.setGraphic(deleteIconView);

                Image downloadIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/download_icon.png")));
                ImageView downloadIconView = new ImageView(downloadIcon);
                downloadIconView.setFitHeight(26);
                downloadIconView.setFitWidth(26);
                downloadButton.setGraphic(downloadIconView);

                editButton.getStyleClass().addAll("wallet-table-button");
                deleteButton.getStyleClass().addAll("wallet-table-button");
                downloadButton.getStyleClass().addAll("wallet-table-button");

                editButton.setOnAction(e -> {
                    walletTable.setEditable(true);
                    getTableView().edit(getIndex(), walletNameColumn);
                });

                deleteButton.setOnAction(e -> {
                    TableWalletInfo item = getTableView().getItems().get(getIndex());
                    walletController.deleteWallet(item.getPublicKey());
                    getTableView().getItems().remove(item);
                    utxoController.updateWalletBalances();
                    walletController.updateWalletNames();
                    SideMenu.getInstance().updateSideMenuWalletList();
                    MiningPanel.getInstance().updateComboBoxOptions();
                });

                downloadButton.setOnAction(e -> {
                    TableWalletInfo item = getTableView().getItems().get(getIndex());
                    String walletJson = walletController.downloadWallet(item.getPublicKey());

                    // File saving
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().add(
                            new FileChooser.ExtensionFilter("Json Files", "*.json")
                    );
                    fileChooser.setInitialFileName(item.getWalletName()/*.replace(" ", "_").toLowerCase()*/ + ".json");

                    File file = fileChooser.showSaveDialog(stage);
                    if (file != null) {
                        try (FileWriter writer = new FileWriter(file)) {
                            writer.write(walletJson);
                        }
                        catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonsBox);
            }
        });

        walletTable.getColumns().addAll(walletNameColumn, walletBalanceColumn, actionsColumn);

        // Add button

        HBox toolBox = new HBox();
        toolBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(toolBox, new Insets(0, 0, 0, 54));

        Image addIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/add_icon.png")));
        ImageView addIconView = new ImageView(addIcon);
        addIconView.setFitHeight(25);
        addIconView.setFitWidth(25);

        Button addWalletButton = new Button("New Wallet", addIconView);
        addWalletButton.getStyleClass().addAll("add-wallet-button");
        addWalletButton.setOnAction(event -> {
            if(walletController.createWallet()){
                walletController.updateWalletNames();
                utxoController.updateWalletBalances();
                wallets = FXCollections.observableArrayList(
                        walletController.getWalletsModel().getWalletNames()
                                .entrySet()
                                .stream()
                                .map(e -> new TableWalletInfo(e.getKey(), e.getValue().get(), utxoController.getWalletBalanceProperty(e.getKey()).get()))
                                .collect(Collectors.toList())
                );
                walletTable.setItems(wallets);
                SideMenu.getInstance().updateSideMenuWalletList();
                MiningPanel.getInstance().updateComboBoxOptions();
            }
            else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to create a new wallet. Please try again.", ButtonType.OK);
                alert.setHeaderText("Error");
                alert.showAndWait();
            }
        });

        Image importIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/import_icon.png")));
        ImageView importIconView = new ImageView(importIcon);
        importIconView.setFitHeight(27);
        importIconView.setFitWidth(27);

        Button importWalletButton = new Button("", importIconView);
        HBox.setMargin(importWalletButton, new Insets(0, 0, 0, 14));
        importWalletButton.getStyleClass().addAll("import-wallet-button");
        importWalletButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Json Files", "*.json")
            );
            File file = fileChooser.showOpenDialog(stage);
            if (file != null) {
                try {
                    walletController.importWallet(file);
                    utxoController.updateWalletBalances();
                    updateWalletTable();
                }
                catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to import the wallet. Please try again.", ButtonType.OK);
                    alert.setHeaderText("Error");
                    alert.showAndWait();
                    e.printStackTrace();
                }
            }
        });

        // Search

        TextField searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.getStyleClass().addAll("wallet-search-field");
        searchField.prefHeightProperty().bind(addWalletButton.heightProperty());
        searchField.prefWidthProperty().bind(this.widthProperty().multiply(.4));
        searchField.setOnKeyTyped(e -> {
            String searchText = searchField.getText().toLowerCase();
            ObservableList<TableWalletInfo> filteredWallets = wallets.stream()
                    .filter(wallet -> wallet.getWalletName().toLowerCase().contains(searchText))
                    .collect(Collectors.toCollection(FXCollections::observableArrayList));
            walletTable.setItems(filteredWallets);
            walletTable.refresh();
        });

        HBox.setMargin(searchField, new Insets(0,0,0,14));

        toolBox.getChildren().addAll(addWalletButton, importWalletButton, searchField);

        walletTable.prefWidthProperty().bind(this.widthProperty().multiply(.897));
        HBox walletTableContainer = new HBox(walletTable);
        walletTableContainer.prefHeightProperty().bind(this.heightProperty().multiply(.772));
        VBox.setMargin(walletTableContainer, new Insets(10, 0, 0, 0));
        walletTableContainer.setAlignment(Pos.CENTER);
        getChildren().addAll(title, toolBox, walletTableContainer);
    }

    public void updateWalletTable(){
        wallets = FXCollections.observableArrayList(
                walletController.getWalletsModel().getWalletNames()
                        .entrySet()
                        .stream()
                        .map(e -> new TableWalletInfo(e.getKey(), e.getValue().get(), utxoController.getWalletBalanceProperty(e.getKey()).get()))
                        .collect(Collectors.toList())
        );
        walletTable.setItems(wallets);
    }

    public static WalletManager getInstance() {
        return instance;
    }
}
