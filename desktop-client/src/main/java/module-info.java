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

    opens org.example.desktopclient to javafx.fxml;
    exports org.example.desktopclient;
    exports org.example.desktopclient.view;
    opens org.example.desktopclient.model to com.fasterxml.jackson.databind, javafx.base;
    opens org.example.desktopclient.view to javafx.fxml;
}