package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

public class Searcher {

    private Ranker ranker;

    /** First string is the number of the query, and the second string is for the format for the trec Evel*/
    private HashMap<String, String> RelevantDoc = new HashMap<>();

    /** for single query only*/
    private HashSet<String> relevDocs = new HashSet<>();

    public void runQueries(String queriesPath) {
        try {
            File file = new File(queriesPath);

            String fileBody = FileUtils.readFileToString(file);
            String[] QueryList = StringUtils.substringsBetween(fileBody, "<top>", "</top>");
            for(int i = 0; i < QueryList.length; i++) {
                String num = StringUtils.substringBetween(QueryList[i], "<num> Number: ", System.lineSeparator());
                String title = StringUtils.substringBetween(QueryList[i], "<title> ", System.lineSeparator());
                RelevantDoc.putAll(ranker.Rank(num, title));
            }
        } catch(Exception e) { e.printStackTrace(); };
    }

    public void runQuery(String query) {
        relevDocs.addAll(ranker.Rank(null, query).values());
    }
}
