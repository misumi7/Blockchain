module org.example.thesisdesktop {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires com.almasb.fxgl.all;
    requires java.net.http;
    requires com.fasterxml.jackson.databind;

    opens org.example.thesisdesktop to javafx.fxml;
    exports org.example.thesisdesktop;
    exports org.example.thesisdesktop.view;
    opens org.example.thesisdesktop.model to com.fasterxml.jackson.databind;
    opens org.example.thesisdesktop.view to javafx.fxml;
}