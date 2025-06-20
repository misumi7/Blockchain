package org.example.desktopclient.view;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Pair;
import org.example.desktopclient.controller.WalletController;

import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class SideMenu extends VBox {
    private static SideMenu instance;
    private WalletController walletController = WalletController.getInstance();
    private Consumer<Pair<String, String>> onSectionSelected;
    private String selectedOption = "";
    private TitledPane wallets = new TitledPane();

    public SideMenu(Consumer<Pair<String, String>> onSectionSelected) {
        instance = this;
        this.onSectionSelected = onSectionSelected;

        getStyleClass().addAll("left-menu");
        //HBox.setMargin(this, new Insets(0, 0, 0, 18));

        Accordion accordion = new Accordion();

        wallets.setText("Wallets");
        wallets.getStyleClass().addAll("menu-component", "wallets");

        ImageView walletArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/icons8-wallet-30.png"))));
        walletArrow.setFitWidth(30);
        walletArrow.setFitHeight(30);
        wallets.setGraphic(walletArrow);

        Platform.runLater(() -> {
            Node arrow = wallets.lookup(".arrow");
            if (arrow != null) {
                arrow.getTransforms().clear();
            }
        });

        //walletsContent.setPadding(new Insets(10));
        Map<String, SimpleStringProperty> walletNames = walletController.getWalletNames();
        VBox walletsContent = new VBox();
        for (String key : walletNames.keySet()) {
            Label walletLabel = new Label();
            walletLabel.textProperty().bind(walletNames.get(key));
            HBox walletButton = new HBox(walletLabel);
            walletButton.getStyleClass().addAll("wallet-button");
            walletButton.setOnMouseClicked(event -> {
                onSectionSelected.accept(new Pair<>(selectedOption, key));
                for (Node node : walletsContent.getChildren()) {
                    node.getStyleClass().remove("selected-option");
                }
                walletButton.getStyleClass().add("selected-option");
            });
            walletsContent.getChildren().add(walletButton);
        }
        walletsContent.getStyleClass().addAll("wallets-content");
        wallets.setContent(walletsContent);


        TitledPane network = new TitledPane();
        network.setText("Network");
        ImageView networkArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/icons8-blockchain-technology-30.png"))));
        networkArrow.setFitWidth(30);
        networkArrow.setFitHeight(30);
        network.setGraphic(networkArrow);
        network.getStyleClass().addAll("menu-component", "network");

        TitledPane node = new TitledPane();
        node.setText("Node");
        ImageView nodeArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/icons8-cube-30.png"))));
        nodeArrow.setFitWidth(30);
        nodeArrow.setFitHeight(30);
        node.setGraphic(nodeArrow);
        node.getStyleClass().addAll("menu-component", "node");

        TitledPane settings = new TitledPane();
        settings.setText("Settings");
        ImageView settingsArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/icons8-settings-30.png"))));
        settingsArrow.setFitWidth(30);
        settingsArrow.setFitHeight(30);
        settings.setGraphic(settingsArrow);
        settings.getStyleClass().addAll("menu-component", "settings", "last-component");


        accordion.getPanes().addAll(
                wallets,
                network,
                node,
                settings
        );

        accordion.expandedPaneProperty().addListener((obs, oldPane, newPane) -> {
            if (newPane != null) {
                for (TitledPane other : accordion.getPanes()) {
                    other.getStyleClass().remove("selected");
                }
                newPane.getStyleClass().addAll("selected");
                selectedOption = newPane.getText();
                onSectionSelected.accept(new Pair<>(selectedOption, ""));
            }
        });

        /*Button homeButton = new Button("Wallets");
        homeButton.getStyleClass().addAll("button");

        Button settingsButton = new Button("Wallets");
        settingsButton.getStyleClass().addAll("button");

        Button logoutButton = new Button("MiningPanel");
        logoutButton.getStyleClass().addAll("button");*/

        /*VBox componentList = new VBox(
                homeButton,
                settingsButton,
                logoutButton
        );

        wallets.setContent(componentList);
        */

        getChildren().addAll(accordion);
    }

    public static SideMenu getInstance() {
        return instance;
    }

    public void updateSideMenuWalletList(){
        Map<String, SimpleStringProperty> walletNames = walletController.getWalletNames();
        VBox walletsContent = new VBox();
        for (String key : walletNames.keySet()) {
            Label walletLabel = new Label();
            walletLabel.textProperty().bind(walletNames.get(key));
            HBox walletButton = new HBox(walletLabel);
            walletButton.getStyleClass().addAll("wallet-button");
            walletButton.setOnMouseClicked(event -> {
                onSectionSelected.accept(new Pair<>(selectedOption, key));
                for (Node node : walletsContent.getChildren()) {
                    node.getStyleClass().remove("selected-option");
                }
                walletButton.getStyleClass().add("selected-option");
            });
            walletsContent.getChildren().add(walletButton);
        }
        walletsContent.getStyleClass().addAll("wallets-content");
        wallets.setContent(walletsContent);
    }

    /*public void setWalletNames(Map<String, String> walletNames) {
        walletsContent.getChildren().clear();
        for (String key : walletNames.keySet()) {
            HBox walletButton = new HBox(new Label(walletNames.get(key)));
            walletButton.getStyleClass().addAll("wallet-button");
            walletButton.setOnMouseClicked(event -> {
                onSectionSelected.accept(new Pair<>(selectedOption, key));
                for (Node node : walletsContent.getChildren()) {
                    node.getStyleClass().remove("selected-option");
                }
                walletButton.getStyleClass().add("selected-option");
            });
            walletsContent.getChildren().add(walletButton);
        }
    }*/
}
