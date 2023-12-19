module Pong {
    requires javafx.controls;
    requires javafx.fxml;


    opens Pong to javafx.fxml;
    exports Pong;
}