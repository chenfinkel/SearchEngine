package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CitiesView {

    private View view;

    @FXML
    private ListView cities;
    @FXML
    private Button select;

    private HashSet<String> searchCities = new HashSet<>();

    public void setView(View view) {
        this.view = view;
    }

    public void setCities() {
        ConcurrentHashMap<String, String> city = view.getCities();
        ObservableList list = FXCollections.observableArrayList();
        if (city.size() == 0) {
            list.add(new Text("No cities found!"));
        } else {
            List<String> sortedLang = new ArrayList<>();
            sortedLang.addAll(city.keySet());
            Collections.sort(sortedLang);
            for (int i = 0; i < sortedLang.size(); i++) {
                CheckBox cb = new CheckBox(sortedLang.get(i));
                cb.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent e) {
                        updateCities((CheckBox) e.getSource());
                    }
                });
                list.add(cb);
            }
            cities.setItems(list);
            cities.setVisible(true);
        }
    }

    private void updateCities(CheckBox cb){
        String city = cb.getText();
        if(cb.isSelected())
            searchCities.add(city);
        else{
            if(searchCities.contains(city))
                searchCities.remove(city);
        }
    }

    public void select(){
        view.setCities(searchCities);
        Stage stage = (Stage)(select.getScene().getWindow());
        stage.close();
    }

}
