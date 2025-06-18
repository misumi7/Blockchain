package org.example.thesisdesktop.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WalletInfo extends VBox {
    public WalletInfo(String walletName) {
        getStyleClass().addAll("wallet-info");
        /*HBox.setMargin(this, new Insets(0, 0, 0, 20));*/

        Label title = new Label(walletName);
        title.getStyleClass().addAll("wallet-title");

        VBox firstSection = new VBox();
        firstSection.getStyleClass().addAll("section");
        firstSection.setSpacing(10);
        firstSection.setPadding(new Insets(10));

        firstSection.setAlignment(Pos.TOP_CENTER);
        firstSection.prefWidthProperty().bind(this.widthProperty().multiply(.9));
        firstSection.maxWidthProperty().bind(this.widthProperty().multiply(.9));
        firstSection.prefHeightProperty().bind(this.heightProperty().multiply(.28));
        HBox.setMargin(firstSection, new Insets(15, 0, 0, 0));

        HBox firstSectionWrapper = new HBox(firstSection);
        firstSectionWrapper.setAlignment(Pos.CENTER);

        getChildren().addAll(title, firstSectionWrapper);
    }
}
