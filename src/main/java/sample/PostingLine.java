package sample;

import java.util.LinkedHashMap;

/**
 * this class represents a line in a posting file
 */
public class PostingLine {

    /**
     * the term in the posting line
     */
    private Term term;

    /**
     * the documents the term appeared in and the frequency
     */
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
