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
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.example.desktopclient.controller.NodeController;
import org.example.desktopclient.controller.WalletController;
import org.example.desktopclient.view.SideMenu;
import org.example.desktopclient.view.MiningPanel;
import org.example.desktopclient.view.WalletInfo;
import org.example.desktopclient.view.WalletManager;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    private final NodeController nodeController = NodeController.getInstance();
    private final WalletController walletController = WalletController.getInstance();

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    public static final String BASE_URL = "http://localhost:8085";

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

        // --
        HBox root = new HBox();
        root.setSpacing(19);
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().addAll("main-container");

        VBox mainContent = new VBox();

        // TEMP:: toAdd: notifications page; :: or set it to settings page and add notifications there
        MiningPanel miningPanel = new MiningPanel();

        WalletManager walletManager = new WalletManager();

        /*walletInfo.prefWidthProperty().bind(root.widthProperty().multiply(.66));
        walletInfo.prefHeightProperty().bind(root.heightProperty());
        mainContent.getChildren().add(walletInfo);*/

        SideMenu sideMenu = new SideMenu((selectedSection) -> {
            Platform.runLater(() -> {
                System.out.println("Selected section: " + selectedSection.getKey() + ", option: " + selectedSection.getValue());
                mainContent.getChildren().clear();
                switch (selectedSection.getKey()) {
                    case "Wallets":
                        switch (selectedSection.getValue()){
                            case "":
                                // TEMP:: to add wallet manager here
                                walletManager.prefWidthProperty().bind(root.widthProperty().multiply(.66));
                                walletManager.prefHeightProperty().bind(root.heightProperty());
                                mainContent.getChildren().add(walletManager);
                                break;
                            default:
                                WalletInfo walletInfo = new WalletInfo(selectedSection.getValue());
                                walletInfo.prefWidthProperty().bind(root.widthProperty().multiply(.66));
                                walletInfo.prefHeightProperty().bind(root.heightProperty());
                                mainContent.getChildren().add(walletInfo);
                        }
                        break;
                    case "Network":
                        mainContent.getChildren().add(new Label("Network Page"));
                        break;
                    case "Node":
                        miningPanel.prefWidthProperty().bind(root.widthProperty().multiply(.66));
                        miningPanel.prefHeightProperty().bind(root.heightProperty());
                        nodeController.updateMempoolTransactionCount(miningPanel);
                        mainContent.getChildren().add(miningPanel);
                        break;
                    case "Settings":
                        mainContent.getChildren().add(new Label("Settings Page"));
                        break;
                }
            });
        });

        // Updates:
        //walletController.updateWalletNames(miningPanel, sideMenu);


        mainContent.prefWidthProperty().bind(root.widthProperty().multiply(.66));
        mainContent.prefHeightProperty().bind(root.heightProperty());

        sideMenu.prefWidthProperty().bind(root.widthProperty().multiply(.3));
        sideMenu.maxHeightProperty().bind(root.heightProperty().multiply(.99));

        root.getChildren().addAll(sideMenu, mainContent);

        Scene scene = new Scene(root, screenWidth / 1.5, screenHeight / 1.5);

        scene.getStylesheets().add(getClass().getResource("styles/sideMenu.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/main.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/walletInfo.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/miningPanel.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/customTitledPane.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/walletManager.css").toExternalForm());

        primaryStage.setTitle("Full Node Client");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // TEMP::
		/*new Thread (() -> {
			SpringApplication.run(Main.class);
		}, "SpringThread").start();*/
    }

    public static void main(String[] args) {
        launch();
    }

    public CompletableFuture<String> getChain() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/nodes/mempool/count"))
                .GET()
                .build();

        return CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body);
    }
}