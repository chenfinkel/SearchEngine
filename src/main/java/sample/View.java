package sample;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import java.io.File;
import java.util.LinkedHashSet;

public class View {
    private Controller control;
    private Stage stage;

    @FXML
    public CheckBox stemming;
    @FXML
    public TextField Corpus;
    @FXML
    public TextField Posting;
    @FXML
    public Button browseCorpus;
    @FXML
    public Button browsePosting;
    @FXML
    public Button startBtn;
    @FXML
    public Button showDict;
    @FXML
    public Button loadDict;
    @FXML
    public ChoiceBox languages;
    @FXML
    public Button resetBtn;
    @FXML
    public TextField queryFile;
    @FXML
    public TextField singleQry;

    public View() {
        control = new Controller();
        control.setView(this);
    }

    public void setController(Controller c) {
        control = c;
    }

    public void Start() {
        if (!Posting.getText().equals("") && !Corpus.getText().equals("")) {
            resetBtn.setDisable(false);
            control.resetSE();
            double time = control.startSE(Corpus.getText(), Posting.getText(), stemming.isSelected());
            LinkedHashSet<String> lang = control.getLanguage();
            languages.setItems(FXCollections.observableArrayList(lang));
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
        startBtn.setDisable(false);
    }

    public void showDict() {
        boolean stem = stemming.isSelected();
        ScrollBar sc = new ScrollBar();
        String dictionary = control.getDictionary(stem);
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
        boolean stem = stemming.isSelected();
        if (!path.equals(""))
            control.loadDict(path, stem);
    }

    public void BrowseQueries(){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Queries Path");
        File f = dc.showDialog(stage);
        if (f != null)
            queryFile.setText(f.getPath());
    }

    public void getEntities(){

    }

    public void Run(){
        String queriesFile = queryFile.getText();
        String query = singleQry.getText();
        if ((queriesFile.equals("") && query.equals("")) ||(!queriesFile.equals("") && !query.equals(""))) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("YOU MUST ENTER A QUERY OR A QUERIES FILE!");
            a.show();
        } else {
            if (!query.equals(""))
                control.RunSingleQuery(query);
            else
                control.RunMultipleQueries(queriesFile);
        }

    }
}
