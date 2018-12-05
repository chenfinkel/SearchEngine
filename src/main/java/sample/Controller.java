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

    public double startSE(String path, String postPath, boolean stem) {
        se.setProps(path,postPath,stem);
        return se.start();
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

    public String getDictionary(){
        return se.getDictionary();
    }

    public void loadDict(String path){
        se.loadDict(path);
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

}
