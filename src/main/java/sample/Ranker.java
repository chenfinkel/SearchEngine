package sample;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Ranker {

    private String postingPath;
    private String corpusPath;

    public Ranker(String postingPath, String corpusPath){
        this.corpusPath = corpusPath;
        this.postingPath = postingPath;
    }

    public HashMap<String, String> Rank(LinkedHashMap<String, Term> dictionary, LinkedHashMap<String, Document> documents, String queryNum, String title, boolean stem){
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

    private double BM25(LinkedHashMap<String, Term> dictionary, LinkedHashMap<String, Document> documents, LinkedHashMap<String, Integer> queryTerms, String docno) {
        double B = 0.75;
        int K = 2;
        Iterator<Map.Entry<String,Integer>> it = queryTerms.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String,Integer> entry = it.next();
            String term = entry.getKey();
            Term t = dictionary.get(term);
            //SEMANTICS !!!!!!!!
            if (t != null){
                try {
                    Stream<String> lines = Files.lines(Paths.get(postingPath + "\\" + Character.toLowerCase(term.charAt(0)) + ".txt"));
                    String line = lines.skip(t.postingLine-1).findFirst().get();
                    String tmp = "";
                    String[] lineSplit = line.split(docno + "*");
                    if(lineSplit.length > 1) {
                        tmp = lineSplit[1];
                        String tfDoc = tmp.split("!")[0];
                        int tfDocNum = Integer.parseInt(tfDoc);
                        int docSize =
                    }

                } catch (Exception e) { e.printStackTrace(); }
            }
        }
        double ans = 0;
        return ans;
    }


}
