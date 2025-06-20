package org.example.desktopclient.view;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.scene.Node;

public class CustomTitledPane extends VBox {
    private final VBox contentBox = new VBox();
    private boolean expanded = false;

    public CustomTitledPane(String title, Image icon, Node content) {
        getStyleClass().addAll("custom-titled-pane");

        HBox header = new HBox(10);
        header.getStyleClass().addAll("titled-pane-header");

        Label titleLabel = new Label(title);
        ImageView iconView = new ImageView(icon);
        iconView.setFitWidth(30);
        iconView.setFitHeight(30);
        header.getChildren().addAll(iconView, titleLabel);

        contentBox.getChildren().add(content);
        contentBox.getStyleClass().addAll("titled-pane-content");
        contentBox.setMaxHeight(0);

        header.setOnMouseClicked((MouseEvent e) -> toggleExpand(header));

        getChildren().addAll(header, contentBox);
    }

    private void toggleExpand(HBox header) {
        expanded = !expanded;

        Timeline timeline = new Timeline();

        if (expanded) {
            contentBox.setManaged(true);
            contentBox.setPrefHeight(0);
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(contentBox.maxHeightProperty(), contentBox.getPrefHeight(), Interpolator.LINEAR))
            );
            timeline.setOnFinished(e -> contentBox.setVisible(true));
        } else {
            timeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(300),
                            new KeyValue(contentBox.prefHeightProperty(), 0, Interpolator.LINEAR))
            );
            timeline.setOnFinished(e -> {
                contentBox.setVisible(false);
                contentBox.setManaged(false);
            });
        }
        timeline.play();
    }
}
