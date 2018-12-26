package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;

public class Searcher {

    private Ranker ranker;

    private String postingPath;

    private String corpusPath;

    /** First string is the number of the query, and the second string is for the format for the trec Evel*/
    private HashMap<String, String> RelevantDoc = new HashMap<>();

    /** for single query only*/
    private HashSet<String> relevDocs = new HashSet<>();

    public Searcher(String postingPath, String corpusPath){
        this.postingPath = postingPath;
        this.corpusPath = corpusPath;
        ranker = new Ranker(postingPath, corpusPath);
    }

    public void runQueries(LinkedHashMap<String, Term> dictionary, String queriesPath, boolean stem) {
        try {
            File file = new File(queriesPath);
            Parse parser = new Parse();
            String fileBody = FileUtils.readFileToString(file);
            String[] QueryList = StringUtils.substringsBetween(fileBody, "<top>", "</top>");
            for(int i = 0; i < QueryList.length; i++) {
                String num = StringUtils.substringBetween(QueryList[i], "<num> Number: ", System.lineSeparator());
                String title = StringUtils.substringBetween(QueryList[i], "<title> ", System.lineSeparator());
                LinkedHashMap<String, Integer> queryTerms = parser.parseQuery(title, stem);
                RelevantDoc.putAll(ranker.Rank(dictionary, num, title, stem));
            }
        } catch(Exception e) { e.printStackTrace(); };
    }

    private String removeStopWords(String title) {
        return"";
    }

    public void runQuery(LinkedHashMap<String, Term> dictionary, String query, boolean stem) {

        relevDocs.addAll(ranker.Rank(dictionary,null, query, stem).values());
    }
}
