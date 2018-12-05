package sample;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

public class Term implements Serializable{

    //the word
    private String id;
    //docFreq represents document frequency of the term
    public int docFreq = 0, termFreq = 0;

    public int postingLine;

    public Term() {}

    public Term(String term) {
        id = term;
    }

    public String toString(){
        return id;
    }

    public void increaseDF(){
        docFreq++;
    }

    public void increaseTF() { termFreq++; }
}
