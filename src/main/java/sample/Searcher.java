package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * this class represents a search engine searcher
 */
public class Searcher {

    /**
     * ranker for calculating documents rank
     */
    private Ranker ranker;
    /**
     * use semantic improvement or not
     */
    private boolean semantic;

    /**
     * the results of the search
     */
    private List<QueryResult> results;

    public Searcher(){
        double avdl = calcAVDL();
        ranker = new Ranker(avdl, semantic);
        results = new ArrayList<>();
    }

    /**
     * run multiple queries
     * @param queriesPath the path og the queries file
     */
    public void runQueries(String queriesPath) {
        try {
            File file = new File(queriesPath);
            String fileBody = FileUtils.readFileToString(file);
            String[] QueryList = StringUtils.substringsBetween(fileBody, "<top>", "</top>");
            for(int i = 0; i < QueryList.length; i++) {
                String num = StringUtils.substringBetween(QueryList[i], "<num> Number: ", System.lineSeparator());
                String title = StringUtils.substringBetween(QueryList[i], "<title> ", System.lineSeparator());
                String description = StringUtils.substringBetween(QueryList[i], "<desc> Description: ", "<narr>");
                description = cleanDescription(description);
                List<Map.Entry<Document,Double>> relevantDocs = ranker.Rank(title + description);
                results.add(new QueryResult(num, relevantDocs));
            }
        } catch(Exception e) { e.printStackTrace(); };
    }

    //clean unneccesary words from the description
    private String cleanDescription(String description){
        String[] splitLine = description.split(System.lineSeparator());
        String noLines = "";
        for (int i = 0; i < splitLine.length; i++)
            noLines = noLines + splitLine[i]+" ";
        String[] splitSpace = noLines.split(" ");
        String des = "";
        for (int i = 0; i < splitSpace.length; i++)
            if (!splitSpace[i].equalsIgnoreCase("identify") && !splitSpace[i].equalsIgnoreCase("documents"))
                des = des + splitSpace[i] + " ";
        return des;
    }

    /**
     * run a single query
     * @param query the query to run
     */
    public void runQuery(String query) {
        QueryResult qr = new QueryResult("");
        qr.setDocuments(ranker.Rank(query));
        results.add(qr);
    }

    //calculate the average length of a document in the corpus
    private double calcAVDL() {
        Iterator<Document> it = SearchEngine.documents.values().iterator();
        int sum = 0;
        while (it.hasNext()) {
            sum = sum + it.next().getSize();
        }
        int size = SearchEngine.documents.size();
        return sum/size;
    }


    public List<QueryResult> getResults() {
        return results;
    }

    public void setSemantic(boolean semantic) {
        this.semantic = semantic;
        ranker.setSemantic(semantic);
    }

    public boolean getSemantic(){
        return semantic;
    }

    public void setCities(HashSet<String> cities) {
        this.ranker.setCities(cities);
    }
}
