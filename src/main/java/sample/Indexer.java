package sample;

import javafx.util.Pair;

import java.lang.management.GarbageCollectorMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class Indexer {

    // Dictionary maps terms to their posting file
    private HashMap<Term, Posting> dictionary;
    //terms maps a term to it's object
    private HashMap<String, Term> terms;
    //termsDocs maps a term to the documents it appeared in
    private HashMap<String, ArrayList<Pair<String, Integer>>> termsDocs;

    public Indexer(){
        dictionary = new HashMap<Term, Posting>();
        terms = new HashMap<String, Term>();
        termsDocs = new HashMap<String, ArrayList<Pair<String, Integer>>>();
    }

    public void Index(HashMap<String, ArrayList<Integer>> docTerms, String DocID) {
        if (docTerms != null) {
            Iterator it = docTerms.keySet().iterator();
            while(it.hasNext()) {
                String termString = (String)it.next();
                if (terms.containsKey(termString)) {
                    Term t = terms.get(termString);
                    t.increaseDF();
                } else {
                    Term t = new Term(termString);
                    terms.put(termString, t);
                    termsDocs.put(termString, new ArrayList<Pair<String, Integer>>());
                }
                int termFreq = docTerms.get(termString).size();
                termsDocs.get(termString).add(new Pair<String, Integer>(DocID,termFreq));
            }
        } else {
            //build posting
            termsDocs = new HashMap<String, ArrayList<Pair<String, Integer>>>();
            terms = new HashMap<String, Term>();
        }
    }

}
