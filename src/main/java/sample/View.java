package sample;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.LinkedHashSet;

public class View {
    private Controller control;
    private Stage stage;

    @FXML
    public javafx.scene.control.CheckBox stemming;
    @FXML
    public javafx.scene.control.TextField Corpus;
    @FXML
    public javafx.scene.control.TextField Posting;
    @FXML
    public javafx.scene.control.Button browseCorpus;
    @FXML
    public javafx.scene.control.Button browsePosting;
    @FXML
    public javafx.scene.control.Button startBtn;
    @FXML
    public javafx.scene.control.Button showDict;
    @FXML
    public javafx.scene.control.Button loadDict;
    @FXML
    public javafx.scene.control.ChoiceBox languages;
    @FXML
    public javafx.scene.control.Button resetBtn;

    public View() {
        control = new Controller();
        control.setView(this);
    }

    public void setController(Controller c) {
        control = c;
    }

    public void Start() {
        if (!Posting.getText().equals("") && !Corpus.getText().equals("")) {
            double time = control.startSE(Corpus.getText(), Posting.getText(), stemming.isSelected());
            resetBtn.setDisable(false);
            LinkedHashSet<String> lang = control.getLanguage();
            //languages.setItems(FXCollections.observableArrayList(lang));
            int terms = control.getNumOfTerms();
            int docs = control.getNumOfDocs();
            final Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initOwner(stage);
            VBox dialogVbox = new VBox(20);
            dialogVbox.getChildren().addAll(new Text("Process finished.\nNumber of unique terms: " + terms + "\n"
            + "Number of documents: " + docs + "\n" + "Total time: " + time + " seconds."));
            Scene dialogScene = new Scene(dialogVbox, 300, 300);
            dialog.setScene(dialogScene);
            dialog.show();

        } else {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("YOU MUST ENTER CORPUS AND POSTING PATHS!");
            a.show();
        }
    }

    public void BrowseCorpus() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Corpus Path");
        File f = dc.showDialog(stage);
        if (f != null)
            Corpus.setText(f.getPath());
    }

    public void BrowsePosting() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Posting Path");
        File f = dc.showDialog(stage);
        if (f != null)
            Posting.setText(f.getPath());
    }

    public void Reset() {
        control.Reset();
    }

    public void showDict() {
        ScrollBar sc = new ScrollBar();
        String dictionary = control.getDictionary();
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().addAll(new Text(dictionary),sc);
        ScrollPane sp = new ScrollPane(dialogVbox);
        Scene dialogScene = new Scene(sp, 300, 500);
        dialog.setScene(dialogScene);
        dialog.show();

    }

    public void loadDict(){
        String path = Posting.getText();
        if (!path.equals(""))
            control.loadDict(path);
    }
}
