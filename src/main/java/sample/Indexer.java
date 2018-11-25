package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Indexer {

    // Dictionary maps terms to their posting file
    private HashMap<Term, Posting> dictionary;

    public void Index(HashMap<String, ArrayList<Integer>> docTerms) {

        Iterator it = docTerms.keySet().iterator();
        Iterator dic = docTerms.keySet().iterator();
    }

}
