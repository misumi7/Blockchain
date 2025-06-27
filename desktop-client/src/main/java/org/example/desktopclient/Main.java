package org.example.desktopclient;

import javafx.application.Application;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.desktopclient.controller.BlockchainController;
import org.example.desktopclient.controller.NodeController;
import org.example.desktopclient.controller.UTXOController;
import org.example.desktopclient.controller.WalletController;
import org.example.desktopclient.model.Block;
import org.example.desktopclient.view.*;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    private BlockchainController blockchainController = BlockchainController.getInstance();
    private final NodeController nodeController = NodeController.getInstance();
    private final WalletController walletController = WalletController.getInstance();
    private final UTXOController utxoController = UTXOController.getInstance();

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    public static final String BASE_URL = "http://localhost:8085";
    public static StackPane root = new StackPane();


    @Override
    public void start(Stage primaryStage) {

        Font.loadFont(getClass().getResourceAsStream("/org/example/desktopclient/fonts/Inconsolata.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/org/example/desktopclient/fonts/Inter.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/org/example/desktopclient/fonts/InterSemiBold.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/org/example/desktopclient/fonts/InterRegular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/org/example/desktopclient/fonts/InterMedium.ttf"), 12);

        //primaryStage.initStyle(StageStyle.UNIFIED);
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double screenWidth = screenBounds.getWidth();
        double screenHeight = screenBounds.getHeight();

        Scene scene = new Scene(root, screenWidth / 1.5, screenHeight / 1.5);

        // --

        HBox mainElements = new HBox();
        mainElements.setSpacing(19);
        mainElements.setAlignment(Pos.TOP_CENTER);
        mainElements.getStyleClass().addAll("main-container");

        VBox mainContent = new VBox();

        // TEMP:: toAdd: notifications page; :: or set it to settings page and add notifications there
        WalletManager walletManager = new WalletManager(primaryStage);
        Network network = new Network(scene, root);
        MiningPanel miningPanel = new MiningPanel();
        Settings settings = Settings.getInstance();
        // TEMP::
        settings.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
        settings.prefHeightProperty().bind(mainElements.heightProperty());
        mainContent.getChildren().add(settings);
        network.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
        network.prefHeightProperty().bind(mainElements.heightProperty());
        // mainContent.getChildren().add(network);
        // ::

        SideMenu sideMenu = new SideMenu((selectedSection) -> {
            Platform.runLater(() -> {
                System.out.println("Selected section: " + selectedSection.getKey() + ", option: " + selectedSection.getValue());
                mainContent.getChildren().clear();
                switch (selectedSection.getKey()) {
                    case "Wallets":
                        network.setPageActive(false);
                        switch (selectedSection.getValue()){
                            case "":
                                walletManager.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
                                walletManager.prefHeightProperty().bind(mainElements.heightProperty());
                                mainContent.getChildren().add(walletManager);
                                walletController.updateWalletNames();
                                utxoController.updateWalletBalances();
                                walletManager.updateWalletTable();
                                break;
                            default:
                                WalletInfo walletInfo = new WalletInfo(root, selectedSection.getValue());
                                walletInfo.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
                                walletInfo.prefHeightProperty().bind(mainElements.heightProperty());
                                mainContent.getChildren().add(walletInfo);
                        }
                        break;
                    case "Network":
                        network.setPageActive(true);
                        network.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
                        network.prefHeightProperty().bind(mainElements.heightProperty());
                        mainContent.getChildren().add(network);
                        break;
                    case "Node":
                        network.setPageActive(false);
                        miningPanel.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
                        miningPanel.prefHeightProperty().bind(mainElements.heightProperty());
                        nodeController.updateMempoolTransactionCount(miningPanel);
                        mainContent.getChildren().add(miningPanel);
                        break;
                    case "Settings":
                        network.setPageActive(false);
                        settings.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
                        settings.prefHeightProperty().bind(mainElements.heightProperty());
                        mainContent.getChildren().add(settings);
                        break;
                }
            });
        });

        // Updates:
        // walletController.updateWalletNames(miningPanel, sideMenu);

        mainContent.prefWidthProperty().bind(mainElements.widthProperty().multiply(.66));
        mainContent.prefHeightProperty().bind(mainElements.heightProperty());

        sideMenu.prefWidthProperty().bind(mainElements.widthProperty().multiply(.3));
        sideMenu.maxHeightProperty().bind(mainElements.heightProperty().multiply(.983));

        mainElements.getChildren().addAll(sideMenu, mainContent);
        root.getChildren().addAll(mainElements);

        scene.getStylesheets().add(getClass().getResource("styles/sideMenu.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/main.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/walletInfo.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/miningPanel.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/customTitledPane.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/walletManager.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/transactionModal.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/createTransactionModal.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/network.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/settings.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/blockModal.css").toExternalForm());

        primaryStage.setTitle("Full Node Client");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setFullScreen(true);
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}