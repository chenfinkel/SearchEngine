package sample;

import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;

import java.awt.event.ActionEvent;
import java.io.File;

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

    public View(){
        control = new Controller();
        control.setView(this);
    }

    public void setController(Controller c) {
        control = c;
    }

    public void Start() {
        control.startSE(Corpus.getText(), Posting.getText(), stemming.isSelected());
    }

    public void BrowseCorpus(){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Corpus Path");
        File f = dc.showDialog(stage);
        Corpus.setText(f.getPath());
    }

    public void BrowsePosting(){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Posting Path");
        File f = dc.showDialog(stage);
        Posting.setText(f.getPath());
    }

}
