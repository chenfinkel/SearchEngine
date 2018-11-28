package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main /*extends Application*/ {

    //@Override
    /*public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }*/


    public static void main(String[] args) {
        // *********** when we will have GUI the user will give the path of the corpus
        ReadFile rf = new ReadFile("C:\\Users\\chenfi\\IdeaProjects\\corpus");
        long StartTime = System.nanoTime();
        rf.read();
        //launch(args);
        long EndTime = System.nanoTime();
        double totalTime = (EndTime - StartTime)/1000000.0;
        System.out.println("Total time:  " + totalTime/60000.0 + " min");
    }
}
