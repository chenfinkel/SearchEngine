package sample;

import javafx.scene.control.Alert;

import java.awt.event.ActionEvent;
import java.util.LinkedHashSet;

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

    public String getDictionary(boolean stem){
        return se.getDictionary(stem);
    }

    public void loadDict(String path, boolean stem){
        se.loadDict(path, stem);
    }

    public LinkedHashSet<String> getLanguage(){
        return se.getLanguage();
    }

    public void Reset() {
        if(se.getPostingPath() != null)
            se.Reset();
        Indexer.numOfTerms = 0;
        Indexer.numOfDocs = 0;
        Indexer.index = 0;
        se = new SearchEngine();
    }


    public void RunSingleQuery(String query) {
        se.RunSingleQuery(query);
    }

    public void RunMultipleQueries(String queryFilePath) {
        se.RunMultipleQueries(queryFilePath);
    }
}
