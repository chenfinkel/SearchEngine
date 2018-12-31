package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class Searcher {

    private Ranker ranker;

    private boolean semantic;


    /** First string is the number of the query, and the second string is for the format for the trec Evel*/
    //private HashSet<QueryResult> RelevantDoc = new HashSet<>();


    private List<QueryResult> results;

    public Searcher(){
        double avdl = calcAVDL();
        ranker = new Ranker(avdl, semantic);
        results = new ArrayList<>();
    }

    public void runQueries(String queriesPath) {
        try {
            File file = new File(queriesPath);
            String fileBody = FileUtils.readFileToString(file);
            String[] QueryList = StringUtils.substringsBetween(fileBody, "<top>", "</top>");
            for(int i = 0; i < QueryList.length; i++) {
                String num = StringUtils.substringBetween(QueryList[i], "<num> Number: ", System.lineSeparator());
                String title = StringUtils.substringBetween(QueryList[i], "<title> ", System.lineSeparator());
                List<Map.Entry<Document,Double>> relevantDocs = ranker.Rank(title);
                results.add(new QueryResult(num, relevantDocs));
            }
        } catch(Exception e) { e.printStackTrace(); };
    }


    public void runQuery(String query) {
        QueryResult qr = new QueryResult("");
        qr.setDocuments(ranker.Rank(query));
        results.add(qr);
    }

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
}
