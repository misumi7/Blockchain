package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.example.desktopclient.Main;
import org.example.desktopclient.controller.WalletController;

import java.util.Objects;

public class Settings extends StackPane {
    private static Settings instance;
    private VBox content = new VBox();
    private VBox notificationsContent = new VBox();
    private Label notificationCount = new Label();

    public Settings(StackPane root) {
        ScrollPane scrollPane = new ScrollPane();

        scrollPane.getStyleClass().add("settings-scroll-pane");
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        content.getStyleClass().add("settings-content");

        // Notifications

        TitledPane notificationsPane = new TitledPane();
        notificationsPane.getStyleClass().add("settings-titled-pane");
        notificationsPane.prefWidthProperty().bind(prefWidthProperty());
        notificationsPane.prefHeightProperty().addListener((observable, oldValue, newValue) -> {
                notificationsPane.setPrefHeight(newValue.doubleValue());
        });

        ImageView notificationsArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/notifications_icon.png"))));
        notificationsArrow.setFitWidth(30);
        notificationsArrow.setFitHeight(30);

        StackPane notificationIconBox = new StackPane(notificationsArrow);
        StackPane.setAlignment(notificationCount, Pos.TOP_RIGHT);
        notificationIconBox.getChildren().add(notificationCount);

        HBox notificationsHeader = new HBox(notificationIconBox, new Label("Notifications"));
        notificationsHeader.setSpacing(8);
        notificationsHeader.setAlignment(Pos.CENTER_LEFT);
        notificationsHeader.setPadding(new Insets(10));

        notificationsPane.setGraphic(notificationsHeader);

        Platform.runLater(() -> {
            Node arrow = notificationsPane.lookup(".arrow");
            if (arrow != null) {
                arrow.getTransforms().clear();
            }
        });

        // Notification content

        notificationsContent.setPadding(Insets.EMPTY);
        notificationsContent.minHeightProperty().set(0);
        notificationsPane.setContent(notificationsContent);
        notificationsPane.setExpanded(notificationsPane.getContent() != null);


        // Settings

        TitledPane settingsPane = new TitledPane();
        settingsPane.getStyleClass().add("settings-titled-pane");
        settingsPane.setExpanded(true);
        settingsPane.prefWidthProperty().bind(prefWidthProperty());
        /*settingsPane.prefHeightProperty().addListener((observable, oldValue, newValue) -> {
            settingsPane.setPrefHeight(newValue.doubleValue());
        });*/

        ImageView settingsArrow = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/settings_icon2.png"))));
        settingsArrow.setFitWidth(30);
        settingsArrow.setFitHeight(30);

        HBox settingsHeader = new HBox(settingsArrow, new Label("Settings"));
        settingsHeader.setSpacing(8);
        settingsHeader.setAlignment(Pos.CENTER_LEFT);
        settingsHeader.setPadding(new Insets(10));

        settingsPane.setGraphic(settingsHeader);

        Platform.runLater(() -> {
            Node arrow = settingsPane.lookup(".arrow");
            if (arrow != null) {
                arrow.getTransforms().clear();
            }
        });

        VBox settingsContent = new VBox();
        settingsContent.setPadding(Insets.EMPTY);
        settingsContent.minHeightProperty().set(0);

        // Change PIN

        HBox changePinItem = new HBox();
        changePinItem.getStyleClass().add("notification-item");
        changePinItem.setAlignment(Pos.CENTER_LEFT);

        Image changePinIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/change_pin_icon.png")));
        ImageView changePinImageView = new ImageView(changePinIcon);
        changePinImageView.setFitWidth(30);
        changePinImageView.setFitHeight(30);
        HBox.setMargin(changePinImageView, new Insets(0, 0, 0, 46));

        Label changePinTitle = new Label("Change PIN");
        changePinTitle.getStyleClass().add("settings-label-title");

        Label changePinDescription = new Label("Update your PIN code");
        changePinDescription.getStyleClass().add("settings-label-description");

        VBox changePinTextBox = new VBox(changePinTitle, changePinDescription);
        HBox.setMargin(changePinTextBox, new Insets(0, 0, 0, 5));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button changePinButton = new Button("Change");
        HBox.setMargin(changePinButton, new Insets(0, 20, 0, 0));
        changePinButton.setOnAction(event -> {
            PinUpdateModal pinUpdateModal = new PinUpdateModal(root);
            pinUpdateModal.prefWidthProperty().bind(root.widthProperty().multiply(.65));
            pinUpdateModal.prefHeightProperty().bind(root.heightProperty().multiply(.5));

            BoxBlur blur = new BoxBlur(3, 3, 2);
            for (Node node : root.getChildren()) {
                node.setEffect(blur);
            }

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), pinUpdateModal);
            pinUpdateModal.setOpacity(0);
            fadeIn.setToValue(1);

            StackPane.setAlignment(pinUpdateModal, Pos.CENTER);
            root.getChildren().addAll(pinUpdateModal);
            fadeIn.play();

            setOnKeyPressed(e -> {
                if (e.getCode().toString().equals("ESCAPE")) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), pinUpdateModal);
                    fadeOut.setToValue(0);
                    fadeOut.setOnFinished(ev -> {
                        root.getChildren().removeAll(pinUpdateModal);
                        for (Node node : root.getChildren()) {
                            node.setEffect(null);
                        }
                    });
                    fadeOut.play();
                }
            });
        });
        changePinButton.getStyleClass().add("settings-button");

        changePinItem.getChildren().addAll(changePinImageView, changePinTextBox, spacer, changePinButton);

        //

        settingsContent.getChildren().addAll(changePinItem);

        settingsPane.setContent(settingsContent);

        //

        content.getChildren().addAll(notificationsPane, settingsPane);
        scrollPane.setContent(content);

        getChildren().addAll(scrollPane);
    }

    public void addNewNotification(String message, boolean isAlert){
        final double[] startX = new double[1]; // as should be final for lambda expressions

        HBox notificationItem = new HBox();
        notificationItem.setOnMousePressed(e -> {
            startX[0] = e.getSceneX();
        });
        notificationItem.setOnMouseDragged(e -> {
            double deltaX = e.getSceneX() - startX[0];
            if (deltaX > 0) {
                notificationItem.setTranslateX(deltaX);
            }
        });
        notificationItem.setOnMouseReleased(e -> {
            double removalThreshold = content.getWidth() / 10;
            if (e.getSceneX() - startX[0] > removalThreshold) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), notificationItem);
                ft.setToValue(0);
                ft.setOnFinished(ev -> {
                    notificationsContent.getChildren().remove(notificationItem);
                    WalletController.getInstance().decNotificationCount();
                });
                ft.play();
            }
            else {
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), notificationItem);
                tt.setToX(0);
                tt.play();
            }
        });

        notificationItem.getStyleClass().add("notification-item");
        notificationItem.setAlignment(Pos.CENTER_LEFT);
        notificationItem.setSpacing(10);

        Image alertIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/org/example/desktopclient/images/" + (isAlert ? "alert_icon_red.png" : "alert_icon_yellow.png"))));
        ImageView alertImageView = new ImageView(alertIcon);
        alertImageView.setFitWidth(25);
        alertImageView.setFitHeight(25);
        HBox.setMargin(alertImageView, new Insets(0, 0, 0, 46));

        Label notificationLabel = new Label(message);
        notificationLabel.getStyleClass().add("notification-label");
        notificationLabel.setWrapText(true);

        notificationItem.getChildren().addAll(alertImageView, notificationLabel);

        notificationsContent.getChildren().add(notificationItem);
    }

    public void bindNotificationCount(SimpleIntegerProperty notificationCountProperty) {
        notificationCountProperty.addListener((observable, oldValue, newValue) -> {
            notificationCount.textProperty().bind(notificationCountProperty.asString());
            notificationCount.visibleProperty().bind(notificationCountProperty.greaterThan(0));
            notificationCount.managedProperty().bind(notificationCountProperty.greaterThan(0));
            notificationCount.getStyleClass().add("notification-count-badge");
        });
    }

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings(Main.root);
        }
        return instance;
    }
}
