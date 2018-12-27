package sample;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class QueryResult {

    private String queryNumber;

    private List<Document> documents;

    public QueryResult(String queryNumber){
        this.queryNumber = queryNumber;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public String toString(){
        String s = "";
        Iterator<Document> it = documents.iterator();
        while(it.hasNext()){
            s = s + queryNumber + " 0 " + it.next().getDocID() + " 1 42.38 mt" + System.lineSeparator();
        }
        return s;
    }
}
