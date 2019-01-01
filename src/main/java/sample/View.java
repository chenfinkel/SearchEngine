package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.util.Pair;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    @FXML
    public Button run;
    @FXML
    public Button browseQueries;
    @FXML
    public CheckBox semantic;
    @FXML
    public Button filterCities;

    private HashSet<String> searchCities;

    public View() {
        control = new Controller();
        control.setView(this);
        searchCities = new HashSet<>();
    }

    public void setController(Controller c) {
        control = c;
    }

    public void setProperties(){
        control.resetSE();
        if (!Posting.getText().equals("") && !Corpus.getText().equals("")) {
            control.setProperties(Corpus.getText(), Posting.getText(), stemming.isSelected());
            startBtn.setDisable(false);
            loadDict.setDisable(false);
        } else {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("YOU MUST ENTER CORPUS AND POSTING PATHS!");
            a.show();
        }
    }

    private void enable(){
        showDict.setDisable(false);
        languages.setDisable(false);
        singleQry.setDisable(false);
        queryFile.setDisable(false);
        browseQueries.setDisable(false);
        filterCities.setDisable(false);
        semantic.setDisable(false);
        run.setDisable(false);
        resetBtn.setDisable(false);
    }

    private void disable(){
        showDict.setDisable(true);
        languages.setDisable(true);
        singleQry.setDisable(true);
        queryFile.setDisable(true);
        browseQueries.setDisable(true);
        filterCities.setDisable(true);
        semantic.setDisable(true);
        run.setDisable(true);
        resetBtn.setDisable(true);
        startBtn.setDisable(true);
        loadDict.setDisable(true);
    }

    public void Start() {
        if (!Posting.getText().equals("") && !Corpus.getText().equals("")) {
            double time = control.startSE();
            enable();
            ConcurrentHashMap<String, String> lang = control.getLanguage();
            languages.setItems(FXCollections.observableArrayList(lang.keySet()));
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
            loadDict.setDisable(true);
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
        disable();
    }

    public void BrowsePosting() {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Posting Path");
        File f = dc.showDialog(stage);
        if (f != null)
            Posting.setText(f.getPath());
        disable();
    }

    public void saveResults(String path){
        control.saveResults(path);
    }

    public void Reset() {
        control.Reset();
        disable();
        Posting.setText("");
        Corpus.setText("");
    }

    public void showDict() {
        ScrollBar sc = new ScrollBar();
        List<Term> dictionary = control.getDictionary();
        final Stage dialog = new Stage();
        dialog.setTitle("Dictionary");
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(0);
        dialogVbox.setPrefHeight(500);
        dialogVbox.setPrefWidth(300);
        TableView<Term> table = new TableView();
        table.setPrefHeight(500);
        table.setPrefWidth(300);
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        TableColumn tc1 = new TableColumn("Term");
        TableColumn tc2 = new TableColumn("Frequency");
        tc1.setCellValueFactory(new PropertyValueFactory<Term, String>("id"));
        tc2.setCellValueFactory(new PropertyValueFactory<Term, String>("termFreq"));
        table.getColumns().addAll(tc1,tc2);
        ObservableList<Term> lines = FXCollections.observableArrayList();
        lines.addAll(dictionary);
        table.setItems(lines);
        dialogVbox.getChildren().addAll(table,sc);
        Scene dialogScene = new Scene(dialogVbox, 300, 500);
        dialog.setScene(dialogScene);
        dialog.show();

    }

    public void loadDict(){
        String path = Posting.getText();
        boolean stem = stemming.isSelected();
        boolean loaded = true;
        if (!path.equals(""))
            loaded = control.loadDict(path, stem);
        if (loaded) {
            enable();
        ConcurrentHashMap<String, String> lang = control.getLanguage();
        languages.setItems(FXCollections.observableArrayList(lang.keySet()));
        startBtn.setDisable(true);
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("Dictionary loaded successfuly");
        a.show();
        } else {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("Not a valid posting folder");
            a.show();
        }
    }

    public void BrowseQueries(){
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose Queries Path");
        File f = fc.showOpenDialog(stage);
        if (f != null)
            queryFile.setText(f.getPath());
    }

    public List<Pair<String, Double>> getEntities(String docID){
        return control.getEntities(docID);
    }

    public void Run(){
        List<QueryResult> results;
        String queriesFile = queryFile.getText();
        String query = singleQry.getText();
        if ((queriesFile.equals("") && query.equals("")) ||(!queriesFile.equals("") && !query.equals(""))) {
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText("YOU MUST ENTER A QUERY OR A QUERIES FILE!");
            a.show();
        } else {
            boolean isSemantic = semantic.isSelected();
            long StartTime = System.nanoTime();
            if (!query.equals(""))
                results = control.RunSingleQuery(query, isSemantic, searchCities);
            else
                results = control.RunMultipleQueries(queriesFile, isSemantic, searchCities);
            long EndTime = System.nanoTime();
            double totalTime = (EndTime - StartTime)/1000000000.0;
            System.out.println("search time: " + totalTime);
            showResults(results);
            searchCities = new HashSet<>();
        }


    }

    private void showResults(List<QueryResult> results){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("results.fxml"));
            Parent root1 = fxmlLoader.load();
            ResultsView viewControl = fxmlLoader.getController();
            viewControl.setView(this);
            viewControl.setResults(results);
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setTitle("Results");
            stage.show();
        }catch (Exception e) { e.printStackTrace(); }
    }

    public ConcurrentHashMap<String, String> getCities(){
        return control.getCities();
    }

    public void setCities(HashSet<String> cities) {
        this.searchCities.addAll(cities);
    }

    public void selectCities(){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("cities.fxml"));
            Parent root1 = fxmlLoader.load();
            CitiesView viewControl = fxmlLoader.getController();
            viewControl.setView(this);
            viewControl.setCities();
            Stage stage = new Stage();
            stage.setScene(new Scene(root1));
            stage.setTitle("Choose cities:");
            stage.show();
        }catch (Exception e) { e.printStackTrace(); }
    }

    public void stemmedPressed(ActionEvent actionEvent) {
        disable();
    }
}
