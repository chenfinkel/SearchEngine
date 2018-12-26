package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("Sample.fxml"));
        primaryStage.setTitle("Our Search Engine - Better Than Google.com");
        primaryStage.setScene(new Scene(root, 697, 641));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
