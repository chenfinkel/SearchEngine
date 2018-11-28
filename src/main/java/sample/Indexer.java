package sample;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;

public class Indexer {

    private int index = 0;
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
    //string- the term, int- df in - docid- docNo
    public void Index(HashMap<String, Integer> docTerms, String DocID) {
        if (docTerms != null) {
            Iterator it = docTerms.keySet().iterator();
            while(it.hasNext()) {
                String termString = (String)it.next();
                int docTermFreq = docTerms.get(termString);
                insert(termString, docTermFreq, DocID);
            }
        } else {
            index++;
            try {
                FileUtils.touch(new File("C:/Users/chenfi/IdeaProjects/posting" + index + ".txt"));
                File file = new File("C:/Users/chenfi/IdeaProjects/posting" + index + ".txt");
                TreeMap<String, Term> sorted = new TreeMap<>();
                sorted.putAll(terms);
                // Hello~3:|D1-3|D2-4|D3-5|/n
                String postingEntry = "";
                String key = "";
                for(Map.Entry<String,Term> entry : sorted.entrySet()) {
                    key = entry.getKey();
                    postingEntry = postingEntry + key + "~" + entry.getValue().docFreq;
                    ArrayList<Pair<String, Integer>> freqs = termsDocs.get(key);
                    for(int i = 0 ; i < freqs.size() ; i++) {
                        postingEntry = postingEntry + "|" + freqs.get(i).getKey() + "-" + freqs.get(i).getValue();
                    }
                    postingEntry = postingEntry + System.lineSeparator();
                }
                FileUtils.writeStringToFile(file, postingEntry);
                termsDocs = new HashMap<String, ArrayList<Pair<String, Integer>>>();
                terms = new HashMap<String, Term>();
            }catch(Exception e){}
        }
    }
    // CHECK UPPERCASE CODE
    private void insert(String termString, int docTermFreq, String DocID) {
        if (termString.equals("")) return;
        if (Character.isUpperCase(termString.charAt(0))) {
            String lowerCase = termString.toLowerCase();
            if (terms.containsKey(lowerCase)) {
                Term t = terms.get(lowerCase);
                t.increaseDF();
                t.termFreq = t.termFreq + docTermFreq;
                termsDocs.get(lowerCase).add(new Pair<String, Integer>(DocID,docTermFreq));
                return;
            } else {
                termString = termString.toUpperCase();
            }
        } else {
            String upperCase = termString.toUpperCase();
            if (terms.containsKey(upperCase))
                terms.remove(upperCase);
        }
        Term t = new Term(termString);
        terms.put(termString, t);
        termsDocs.put(termString, new ArrayList<Pair<String, Integer>>());
        termsDocs.get(termString).add(new Pair<String, Integer>(DocID,docTermFreq));
    }
}
