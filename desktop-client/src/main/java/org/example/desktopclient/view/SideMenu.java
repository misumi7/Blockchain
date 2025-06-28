package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.Label;
import javafx.scene.control.TitledPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;
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

        wallets.setText("");
        wallets.getStyleClass().addAll("menu-component", "wallets");
        VBox walletsContent = new VBox();

        ImageView walletArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/icons8-wallet-30.png"))));
        walletArrow.setFitWidth(30);
        walletArrow.setFitHeight(30);
        //wallets.setGraphic(walletArrow);

        Platform.runLater(() -> {
            Node arrow = wallets.lookup(".arrow");
            if (arrow != null) {
                arrow.getTransforms().clear();
            }
        });

        HBox walletsHeader = new HBox();
        walletsHeader.prefWidthProperty().bind(wallets.widthProperty().multiply(.9));
        walletsHeader.setAlignment(Pos.CENTER_LEFT);
        //walletsHeader.setPadding(new Insets(10, 0, 0, 10));

        Label walletsLabel = new Label("Wallets");
        walletsLabel.getStyleClass().addAll("wallets-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Image walletManagerIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/wallet_manager_icon.png")));
        ImageView walletManagerImageView = new ImageView(walletManagerIcon);
        walletManagerImageView.setOpacity(0);
        walletManagerImageView.setMouseTransparent(false);
        walletManagerImageView.setPickOnBounds(true);

        HBox.setMargin(walletManagerImageView, new Insets(0, 10, 0, 0));
        walletManagerImageView.setFitWidth(30);
        walletManagerImageView.setFitHeight(30);

        walletManagerImageView.setOnMousePressed(Event::consume);
        walletManagerImageView.setOnMouseClicked(e -> {
            e.consume();
            wallets.setExpanded(true);
            selectedOption = walletsLabel.getText();
            for (Node node : walletsContent.getChildren()) {
                node.getStyleClass().remove("selected-option");
            }
            onSectionSelected.accept(new Pair<>(selectedOption, ""));
        });

        walletsHeader.getChildren().addAll(walletArrow, walletsLabel, spacer, walletManagerImageView);

        wallets.setGraphic(walletsHeader);

        //walletsContent.setPadding(new Insets(10));
        Map<String, SimpleStringProperty> walletNames = walletController.getWalletNames();
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
        settings.getStyleClass().addAll("menu-component", "settings", "last-component", "selected");


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
                String newOption = newPane.getText().isEmpty() ? ((Label)((HBox)newPane.getGraphic()).getChildren().get(1)).getText() : newPane.getText();
                if(!selectedOption.equals(newOption)){
                    if(newOption.equals(walletsLabel.getText())){
                        fadeIn(walletManagerImageView);
                    }
                    else{
                        fadeOut(walletManagerImageView);
                    }
                }
                selectedOption = newOption;
                if(!selectedOption.equals(walletsLabel.getText())){
                    for (Node wallet : walletsContent.getChildren()) {
                        wallet.getStyleClass().remove("selected-option");
                    }
                }
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

    public void fadeIn(ImageView imageView) {
        imageView.setOpacity(0.0);
        imageView.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(200), imageView);
        ft.setFromValue(0.0);
        ft.setToValue(.85);
        ft.play();
    }

    public void fadeOut(ImageView imageView) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), imageView);
        ft.setFromValue(.85);
        ft.setToValue(.0);
        ft.setOnFinished(e -> imageView.setVisible(false));
        ft.play();
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
