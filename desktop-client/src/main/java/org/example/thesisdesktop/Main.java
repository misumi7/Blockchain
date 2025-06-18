package org.example.thesisdesktop;

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
import org.example.thesisdesktop.controller.NodeController;
import org.example.thesisdesktop.controller.WalletController;
import org.example.thesisdesktop.view.LeftMenu;
import org.example.thesisdesktop.view.Node;
import org.example.thesisdesktop.view.WalletInfo;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class Main extends Application {
    private final NodeController nodeController = NodeController.getInstance();
    private final WalletController walletController = WalletController.getInstance();

    private static final HttpClient CLIENT = HttpClient.newHttpClient();
    public static final String BASE_URL = "http://localhost:8085";

    @Override
    public void start(Stage primaryStage) {
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inconsolata.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream("/fonts/Inter.ttf"), 12);

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
        Node node = new Node();

        LeftMenu leftMenu = new LeftMenu(selectedSection -> {
            Platform.runLater(() -> {
                mainContent.getChildren().clear();
                switch (selectedSection) {
                    case "Wallets":
                        WalletInfo walletInfo = new WalletInfo("Wallet #hqiW+8u");
                        walletInfo.prefWidthProperty().bind(root.widthProperty().multiply(.66));
                        walletInfo.prefHeightProperty().bind(root.heightProperty());
                        mainContent.getChildren().add(walletInfo);
                        break;
                    case "Network":
                        mainContent.getChildren().add(new Label("Network Page"));
                        break;
                    case "Node":
                        node.prefWidthProperty().bind(root.widthProperty().multiply(.66));
                        node.prefHeightProperty().bind(root.heightProperty());
                        nodeController.updateMempoolTransactionCount(node);
                        mainContent.getChildren().add(node);
                        break;
                    case "Settings":
                        mainContent.getChildren().add(new Label("Settings Page"));
                        break;
                }
            });
        });

        // Updates:
        walletController.updateWalletNames(node, leftMenu);


        mainContent.prefWidthProperty().bind(root.widthProperty().multiply(.66));
        mainContent.prefHeightProperty().bind(root.heightProperty());

        leftMenu.prefWidthProperty().bind(root.widthProperty().multiply(.3));
        leftMenu.maxHeightProperty().bind(root.heightProperty().multiply(.99));

        root.getChildren().addAll(leftMenu, mainContent);

        Scene scene = new Scene(root, screenWidth / 1.5, screenHeight / 1.5);

        scene.getStylesheets().add(getClass().getResource("styles/leftMenu.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/main.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/walletInfo.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/node.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/customTitledPane.css").toExternalForm());

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