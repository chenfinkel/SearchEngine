package sample;

import java.util.*;

/**
 * this class reprsents a result for a query
 */
public class QueryResult {

    /**
     * the number of the query
     */
    private String queryNumber;

    /**
     * the documents resulted as relevant for the query
     */
    private List<Map.Entry<Document,Double>> documents;

    public QueryResult(){
        documents = new ArrayList<>();
    }

    public QueryResult(String queryNumber){
        this.queryNumber = queryNumber;
        this.documents = new ArrayList<>();
    }

    public QueryResult(String queryNumber, List<Map.Entry<Document,Double>> documents){
        this.queryNumber = queryNumber;
        this.documents = new ArrayList<>();
        this.documents.addAll(documents);
    }

    public void setDocuments(List<Map.Entry<Document,Double>> documents) {
        this.documents.addAll(documents);
    }

    public List<Map.Entry<Document,Double>> getDocuments() {
        return documents;
    }

    public String getQueryNumber() {
        return queryNumber;
    }

    public String toString(){
        String s = "Query: " + queryNumber + System.lineSeparator();
        Iterator<Map.Entry<Document,Double>> it = documents.iterator();
        while(it.hasNext()){
            s = s + it.next().getKey().getDocID() + System.lineSeparator();
        }
        return s;
    }
}
