package sample;

import javafx.scene.control.Alert;
import javafx.util.Pair;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Controller {
    private SearchEngine se;
    private View view;


    public Controller(){
        se = new SearchEngine();

    }

    public void resetSE(){
        se = new SearchEngine();
    }

    public double startSE() {
        return se.start();
    }

    public void setProperties(String path, String postPath, boolean stem){
        se.setProps(path,postPath,stem);
    }

    public int getNumOfTerms(){
        return se.getNumOfTerms();
    }

    public int getNumOfDocs(){
        return se.getNumOfDocs();
    }

    public void setView(View v) {
        view = v;
    }

    public List<Term> getDictionary(){
        return se.getDictionary();
    }

    public boolean loadDict(String path, boolean stem){
        return se.loadDict(path, stem);
    }

    public ConcurrentHashMap<String, String> getLanguage(){
        return se.getLanguage();
    }

    public void Reset() {
        if(se.getPostingPath() != null)
            se.Reset();
        Indexer.index = 0;
        se = new SearchEngine();
    }


    public List<QueryResult> RunSingleQuery(String query, boolean semantic, HashSet<String> cities) {
        return se.RunSingleQuery(query, semantic, cities);
    }

    public List<QueryResult> RunMultipleQueries(String queryFilePath, boolean semantic, HashSet<String> cities) {
        return se.RunMultipleQueries(queryFilePath, semantic, cities);
    }

    public List<Pair<String, Double>> getEntities(String docID) {
        return se.getEntities(docID);
    }

    public ConcurrentHashMap<String,City> getCities() {
        return se.cities;
    }

    public void saveResults(String path) {
        se.saveResults(path);
    }
}
