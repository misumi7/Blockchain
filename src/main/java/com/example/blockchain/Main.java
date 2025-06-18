package com.example.blockchain;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.RestController;

import java.security.Security;

@SpringBootApplication
@RestController
@EnableScheduling // enables auto execution of @Scheduled methods
public class Main {
	//private ConfigurableApplicationContext context;

	public static void main(String[] args) {
		Security.addProvider(new BouncyCastleProvider());
		//Application.launch(Main.class, args);
		SpringApplication.run(Main.class, args);
	}

	/*@Override
	public void start(Stage primaryStage) {
		Font.loadFont(getClass().getResourceAsStream("/fonts/Inconsolata.ttf"), 12);
		Font.loadFont(getClass().getResourceAsStream("/fonts/Inter.ttf"), 12);

		//primaryStage.initStyle(StageStyle.UNIFIED);
		Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
		double screenWidth = screenBounds.getWidth();
		double screenHeight = screenBounds.getHeight();

		// --
		HBox root = new HBox();
		root.setAlignment(Pos.TOP_CENTER);
		root.getStyleClass().addAll("main-container");

		LeftMenu leftMenu = new LeftMenu();
		leftMenu.prefWidthProperty().bind(root.widthProperty().multiply(.3));
		leftMenu.maxHeightProperty().bind(root.heightProperty().multiply(.99));

		WalletInfo walletInfo = new WalletInfo("Wallet #hqiW+8u");
		walletInfo.prefWidthProperty().bind(root.widthProperty().multiply(.66));
		walletInfo.maxHeightProperty().bind(root.heightProperty());

		root.getChildren().addAll(leftMenu, walletInfo);

		Scene scene = new Scene(root, screenWidth / 1.5, screenHeight / 1.5);

		scene.getStylesheets().add(getClass().getResource("/ui/styles/leftMenu.css").toExternalForm());
		scene.getStylesheets().add(getClass().getResource("/ui/styles/main.css").toExternalForm());
		scene.getStylesheets().add(getClass().getResource("/ui/styles/walletInfo.css").toExternalForm());
		scene.getStylesheets().add(getClass().getResource("/ui/styles/section.css").toExternalForm());

		primaryStage.setTitle("AAaaaAAAaaaAaaaaAAAAaa");
		primaryStage.setScene(scene);
		primaryStage.setMaximized(true);
		primaryStage.show();*/

		// TEMP:: for the sake of time
		/*new Thread (() -> {
			SpringApplication.run(Main.class);
		}, "SpringThread").start();
	}*/
}
