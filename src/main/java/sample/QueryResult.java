package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class QueryResult {

    private String queryNumber;

    private List<Document> documents;

    public QueryResult(){
        documents = new ArrayList<>();
    }

    public QueryResult(String queryNumber){
        this.queryNumber = queryNumber;
        this.documents = new ArrayList<>();
    }

    public QueryResult(String queryNumber, List<Document> documents){
        this.queryNumber = queryNumber;
        this.documents = new ArrayList<>();
        this.documents.addAll(documents);
    }

    public void setDocuments(List<Document> documents) {
        this.documents.addAll(documents);
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String getQueryNumber() {
        return queryNumber;
    }

    public String toString(){
        String s = "Query: " + queryNumber + System.lineSeparator();
        Iterator<Document> it = documents.iterator();
        while(it.hasNext()){
            s = s + it.next().getDocID() + System.lineSeparator();
        }
        return s;
    }
}
