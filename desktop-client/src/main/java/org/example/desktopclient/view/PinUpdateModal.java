package org.example.desktopclient.view;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class PinUpdateModal extends StackPane {
    public PinUpdateModal(StackPane root){
        VBox background = new VBox();
        background.prefWidthProperty().bind(prefWidthProperty());
        background.getStyleClass().addAll("transaction-modal");
        //setAlignment(Pos.CENTER);

        // Content

        VBox content = new VBox();
        content.setSpacing(10);
        content.prefWidthProperty().bind(background.prefWidthProperty().multiply(.3));
        content.getStyleClass().addAll("transaction-content");

        Label title = new Label("PIN Update:");
        title.getStyleClass().addAll("transaction-title");

        // Old pin

        Label oldPinLabel = new Label("Enter your old PIN:");
        VBox.setMargin(oldPinLabel, new Insets(0, 0, 5, 0));
        oldPinLabel.getStyleClass().addAll("update-pin-label");

        HBox oldPinBox = new HBox();
        oldPinBox.setSpacing(9);
        //oldPinBox.setAlignment(Pos.CENTER);
        List<TextField> oldPinFields = new ArrayList<>();
        for(int i = 0; i < 6; ++i){
            TextField pinField = new TextField();
            if(i == 0){
                Platform.runLater(pinField::requestFocus);
            }
            pinField.prefWidthProperty().bind(content.widthProperty().multiply(.115));
            pinField.minHeightProperty().bind(content.heightProperty().multiply(.136));
            pinField.maxHeightProperty().bind(content.heightProperty().multiply(.136));
            pinField.getStyleClass().addAll("create-transaction-pin-field");

            pinField.setPromptText("*");
            pinField.setTextFormatter(new TextFormatter<>(change ->
                    change.getControlNewText().matches("\\d{0,1}") ? change : null));

            int ind = i;
            pinField.textProperty().addListener((obs, oldValue, newValue) -> {
                if(!newValue.isEmpty() && ind < 5) {
                    oldPinFields.get(ind + 1).requestFocus();
                }
            });

            pinField.setOnKeyPressed(e -> {
                if(e.getCode() == KeyCode.BACK_SPACE && pinField.getText().isEmpty() && ind > 0) {
                    oldPinFields.get(ind - 1).requestFocus();
                }
            });

            oldPinFields.add(pinField);
            oldPinBox.getChildren().add(pinField);
        }

        // New pin

        Label newPinLabel = new Label("Enter your new PIN:");
        VBox.setMargin(newPinLabel, new Insets(5, 0, 5, 0));
        oldPinLabel.getStyleClass().addAll("update-pin-label");

        HBox newPinBox = new HBox();
        newPinBox.setSpacing(9);
        //newPinBox.setAlignment(Pos.CENTER);
        List<TextField> newPinFields = new ArrayList<>();
        for(int i = 0; i < 6; ++i){
            TextField pinField = new TextField();
            pinField.prefWidthProperty().bind(content.widthProperty().multiply(.115));
            pinField.minHeightProperty().bind(content.heightProperty().multiply(.136));
            pinField.maxHeightProperty().bind(content.heightProperty().multiply(.136));
            pinField.getStyleClass().addAll("create-transaction-pin-field");

            pinField.setPromptText("*");
            pinField.setTextFormatter(new TextFormatter<>(change ->
                    change.getControlNewText().matches("\\d{0,1}") ? change : null));

            int ind = i;
            pinField.textProperty().addListener((obs, oldValue, newValue) -> {
                if(!newValue.isEmpty() && ind < 5) {
                    newPinFields.get(ind + 1).requestFocus();
                }
            });

            pinField.setOnKeyPressed(e -> {
                if(e.getCode() == KeyCode.BACK_SPACE && pinField.getText().isEmpty() && ind > 0) {
                    newPinFields.get(ind - 1).requestFocus();
                }
            });

            newPinFields.add(pinField);
            newPinBox.getChildren().add(pinField);
        }

        // Update button

        Button updateButton = new Button("Update");
        updateButton.getStyleClass().addAll("update-pin-button");
        updateButton.setOnAction(e -> {
            String oldPin = oldPinFields.stream().map(TextField::getText).reduce("", String::concat);
            String newPin = newPinFields.stream().map(TextField::getText).reduce("", String::concat);

            System.out.println("Old PIN: " + oldPin);
            System.out.println("New PIN: " + newPin);

            if(oldPin.length() == 6 || newPin.length() == 6) {
                // send request to the controller

                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    root.getChildren().removeAll(this);
                    for (Node node : root.getChildren()) {
                        node.setEffect(null);
                    }
                });
                fadeOut.play();
            }
        });

        HBox updateButtonWrapper = new HBox(updateButton);
        VBox.setMargin(updateButtonWrapper, new Insets(11, 0, 0, 0));
        updateButtonWrapper.setAlignment(Pos.CENTER);

        VBox oldPinContainer = new VBox(oldPinLabel, oldPinBox);
        oldPinBox.setSpacing(8);

        VBox newPinContainer = new VBox(newPinLabel, newPinBox);
        newPinBox.setSpacing(8);

        content.getChildren().addAll(title, oldPinContainer, newPinContainer, updateButtonWrapper);

        HBox contentWrapper = new HBox(content);
        content.maxHeightProperty().bind(prefHeightProperty().multiply(.535));
        contentWrapper.setAlignment(Pos.CENTER);

        contentWrapper.setOnMouseClicked(e -> {
            if(e.getTarget() == contentWrapper) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), this);
                fadeOut.setToValue(0);
                fadeOut.setOnFinished(ev -> {
                    root.getChildren().removeAll(this);
                    for(Node node : root.getChildren()) {
                        node.setEffect(null);
                    }
                });
                fadeOut.play();
            }
        });

        getChildren().addAll(background, contentWrapper);
    }
}
