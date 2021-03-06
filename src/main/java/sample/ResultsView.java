package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.File;
import java.util.*;

/**
 * this class represents a result display window
 */
public class ResultsView {

    /**
     * the main view of the program
     */
    private View view;

    private Stage stage;

    @FXML
    private ListView results;
    @FXML
    public TextField Results;
    @FXML
    public Button browseResults;

    /**
     * documents retrived as relevant
     */
    private ArrayList<String> docs = new ArrayList<>();

    /**
     * matching buttons for the documents retrived, to get primary entities
     */
    private ArrayList<Button> buttons = new ArrayList<>();



    public void setView(View view) {
        this.view = view;
    }

    public void setResults(List<QueryResult> res){
        results.setVisible(false);
        ObservableList resultLines = FXCollections.observableArrayList();
        if(res.size()==0){
            resultLines.add("No documents found!");
        }
        else {
            for (int i = 0; i < res.size(); i++) {
                ListView l = new ListView();
                QueryResult qr = res.get(i);
                resultLines.add("Query: " + qr.getQueryNumber());
                Iterator<Map.Entry<Document,Double>> it = qr.getDocuments().iterator();
                while(it.hasNext()){
                    HBox hbox = new HBox();
                    Label label = new Label(it.next().getKey().getDocID());
                    docs.add(label.getText());
                    Button btn = new Button("Show Primary Entities");
                    btn.setOnAction(new EventHandler<ActionEvent>() {
                        @Override public void handle(ActionEvent e) {
                            getEntities((Button)e.getSource());
                        }
                    });
                    buttons.add(btn);
                    btn.setLayoutY(label.getHeight());
                    hbox.setSpacing(20);
                    hbox.setHgrow(label, Priority.ALWAYS);
                    hbox.setHgrow(btn, Priority.ALWAYS);
                    hbox.getChildren().addAll(label, btn);
                    resultLines.add(hbox);
                }
            }
        }
        results.setItems(resultLines);
        results.setVisible(true);
    }

    //return the primary entities of a document by its matching button
    private void getEntities(Button b){
        String docID = docs.get(buttons.indexOf(b));
        List<Pair<String, Double>> entities = view.getEntities(docID);
        String s = "";
        for (int i = 0; i < entities.size(); i++){
            s = s + "Term: " + entities.get(i).getKey() + ", Grade: " + entities.get(i).getValue() + System.lineSeparator();
        }
        final Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initOwner(stage);
        VBox dialogVbox = new VBox(20);
        dialogVbox.getChildren().addAll(new Text(s));
        Scene dialogScene = new Scene(dialogVbox, 300, 300);
        dialog.setScene(dialogScene);
        dialog.setTitle("Primary Entities");
        dialog.show();
    }

    /**
     * saved the results to a specified path
     * @param actionEvent
     */
    public void saveResults(ActionEvent actionEvent) {
        view.saveResults(Results.getText());
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText("Results saved");
        a.show();
    }

    /**
     * browse a path for saving results
     */
    public void browseResults(){
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Choose Results Path");
        File f = dc.showDialog(stage);
        if (f != null)
            Results.setText(f.getPath());
    }
}
