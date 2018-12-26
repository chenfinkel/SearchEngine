package sample;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;

public class Ranker {

    private String postingPath;
    private String corpusPath;

    public Ranker(String postingPath, String corpusPath){
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
    }

    public HashMap<String, String> Rank(LinkedHashMap<String, Term> dictionary, String queryNum, String title, boolean stem){
        try {
            HashMap<String, String> docs = new HashMap<>();
            FileReader fr = new FileReader(postingPath + "\\docs.txt");
            BufferedReader br = new BufferedReader(fr);
            Parse p = new Parse();
            String line = br.readLine();
            while (line != null) {
                String docno = line.split("~")[0];
                LinkedHashMap<String, Integer> queryTerms = p.parseQuery(queryNum, stem);
                BM25(dictionary, queryTerms, docno);
            }
            return docs;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    private double BM25(LinkedHashMap<String, Term> dictionary, LinkedHashMap<String, Integer> queryTerms, String docno) {
        double B = 0.75;
        int K = 2;
        Iterator it = queryTerms.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,String> entry = (Map.Entry<String,String>)it.next();
            String term = entry.getKey();
            Term t = dictionary.get(term);
            //SEMANTICS !!!!!!!!
            if (t != null){
                try {
                    FileReader fr = new FileReader(postingPath + Character.toLowerCase(term.charAt(0)) + ".txt");
                }catch (Exception e) { e.printStackTrace(); }
            }
        }
        double ans = 0;
        return ans;
    }
}
