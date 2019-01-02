package sample;

import javafx.scene.control.Alert;
import javafx.util.Pair;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * this class represents the main controller of the program
 */
public class Controller {
    /**
     * the model of the program
     */
    private SearchEngine se;

    /**
     * the main view of the program
     */
    private View view;

    /**
     * constructor
     */
    public Controller(){
        se = new SearchEngine();

    }

    /**
     * resets the engine
     */
    public void resetSE(){
        se = new SearchEngine();
    }

    /**
     * start index process
     * @return the time of the process
     */
    public double startSE() {
        return se.start();
    }

    /**
     * sets the engine properties
     * @param path the path of the corpus
     * @param postPath the path of the posting files
     * @param stem if stemming
     */
    public void setProperties(String path, String postPath, boolean stem){
        se.setProps(path,postPath,stem);
    }

    /**
     *
     * @return the number of terms in the dictionary
     */
    public int getNumOfTerms(){
        return se.getNumOfTerms();
    }

    /**
     *
     * @return the number of docs in the corpus
     */
    public int getNumOfDocs(){
        return se.getNumOfDocs();
    }

    /**
     * setter
     * @param v a view  class
     */
    public void setView(View v) {
        view = v;
    }

    /**
     *
     * @return the dictionary of the engine
     */
    public List<Term> getDictionary(){
        return se.getDictionary();
    }

    /**
     * loads the dictionary of the engine from the disk to main memory
     * @param path the path of the posting files
     * @param stem if stemming
     * @return if succeeded or not
     */
    public boolean loadDict(String path, boolean stem){
        return se.loadDict(path, stem);
    }

    /**
     *
     * @return the languages of the documents in the corpus
     */
    public ConcurrentHashMap<String, String> getLanguage(){
        return se.getLanguage();
    }

    /**
     * reset the engine
     */
    public void Reset() {
        if(se.getPostingPath() != null)
            se.Reset();
        Indexer.index = 0;
        se = new SearchEngine();
    }

    /**
     * run a single query
     * @param query the query to run
     * @param semantic use semantic improvement
     * @param cities cities to filter by
     * @return the results for the query
     */
    public List<QueryResult> RunSingleQuery(String query, boolean semantic, HashSet<String> cities) {
        return se.RunSingleQuery(query, semantic, cities);
    }

    /**
     * run a multiple queries file
     * @param queryFilePath the path of the queries file
     * @param semantic use semantic improvement
     * @param cities cities to filter by
     * @return the results for the queries
     */
    public List<QueryResult> RunMultipleQueries(String queryFilePath, boolean semantic, HashSet<String> cities) {
        return se.RunMultipleQueries(queryFilePath, semantic, cities);
    }

    /**
     * get primary entities of a document
     * @param docID the document to get primary entities of
     * @return the primary entities of the document
     */
    public List<Pair<String, Double>> getEntities(String docID) {
        return se.getEntities(docID);
    }

    /**
     *
     * @return the cities of the documents
     */
    public ConcurrentHashMap<String,City> getCities() {
        return se.cities;
    }

    /**
     * save query results to a file
     * @param path the path to save the results to
     */
    public void saveResults(String path) {
        se.saveResults(path);
    }
}
