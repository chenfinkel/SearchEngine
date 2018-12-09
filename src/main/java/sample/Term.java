package sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

/** this class represents a term in the corpus */
public class Term implements Serializable{

    /** the string of the term */
    private String id;

    /** the number of documents the term appeared in*/
    public int docFreq = 0;

    /** the total sum of the term's appearances in the whole corpus */
    public int termFreq = 0;

    /** the line of the posting file */
    public int postingLine;

    /** empty constructor */
    public Term() {}

    /** constructor */
    public Term(String term) {
        id = term;
    }
    /**constructor */
    public Term(String term, int termFreq, int docFreq, int postingLine){
        id = term;
        this.termFreq = termFreq;
        this.docFreq = docFreq;
        this.postingLine = postingLine;
    }

    public String toString(){
        return id;
    }

    /** increase document frequency */
    public void increaseDF(){
        docFreq++;
    }

    /** increase term frequency */
    public void increaseTF() { termFreq++; }
}
