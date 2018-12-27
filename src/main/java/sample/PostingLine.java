package sample;

import java.util.LinkedHashMap;

public class PostingLine {

    private Term term;

    private LinkedHashMap<String, Integer> documents;

    public void setDocuments(LinkedHashMap<String, Integer> documents) {
        this.documents = documents;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public Term getTerm() {
        return term;
    }

    public LinkedHashMap<String, Integer> getDocuments() {
        return documents;
    }
}
