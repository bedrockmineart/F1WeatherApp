module com.f1weather {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;
    requires jdk.compiler;

    opens com.f1weather to javafx.fxml;
    exports com.f1weather;
}