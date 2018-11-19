package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

public class Parse {

    private Indexer idxr;

    public Parse() {
        idxr = new Indexer();
    }

    /**
     *
     * @param text is the document text
     * @param id is the id of the document
     */

    public void ParseDoc(String text, String id) {
        //HashMap contains the terms of the document, and the location
        HashMap<Term, Integer> docTerms = new HashMap<>();
        StringTokenizer st = new StringTokenizer(text, " ,!;.()[]{}?<>/\"");
        while(st.hasMoreTokens())
            System.out.println(st.nextToken());






    }
}
