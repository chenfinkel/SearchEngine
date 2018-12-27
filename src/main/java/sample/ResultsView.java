package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.ArrayList;
import java.util.List;

public class ResultsView {

    private View view;

    @FXML
    private ListView results;

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
            for (int i = 0; i < res.size(); i++)
                resultLines.add(res.get(i).toString());
        }
        results.setItems(resultLines);
        results.setVisible(true);
    }

}
