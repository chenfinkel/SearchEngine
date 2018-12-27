package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.decimal4j.util.DoubleRounder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/** This class represents a search engine */

public class SearchEngine {

    /** the location of the corpus */
    private String corpusPath;

    /** the path where the index files will be saved */
    private String postingPath;

    /** indicates if the parsing process is done with stemming or not */
    private boolean stem;

    /** reads the corpus */
    private ReadFile readFile;

    /** the dictionary of the search engine after prasing and indexing */
    public LinkedHashMap<String, Term> dictionary;

    public LinkedHashMap<String, Document> documents;

    /** the searcher of the engine */
    private Searcher searcher;

    /** constructor */
    public SearchEngine() {
        new File("C:\\TempFiles").mkdirs();
        new File("C:\\TempFiles\\posting").mkdirs();
        new File("C:\\TempFiles\\docs").mkdirs();
        new File("C:\\TempFiles\\city").mkdirs();
        new File("C:\\TempFiles\\languages").mkdirs();
        new File("C:\\TempFiles\\DocTF").mkdirs();
        Indexer.index = 0;
        Indexer.numOfDocs = 0;
        Indexer.numOfTerms = 0;
    }

    /** set the properties for the search engine
     *
     * @param cp - the location of the corpus
     * @param pp - the location for the index to be saved
     * @param stem - indicates if the parse will we with stemming or not
     */
    public void setProps(String cp, String pp, boolean stem) {
        corpusPath = cp;
        postingPath = pp;
        this.stem = stem;
        readFile = new ReadFile(cp, pp, stem);
        searcher = new Searcher(pp,cp,documents);

    }

    /** get the location of the index */
    public String getPostingPath() {
        return postingPath;
    }

    /**
     * loads the dictionary from the disk to the main memory
     * @param path - the location of the dictionary
     * @param stem - is stemmed or not
     */
    public void loadDict(String path, boolean stem){
        loadDictionary(path,stem);
        loadDocs(path,stem);
    }

    private void loadDocs(String path, boolean stem){
        try {
            documents = new LinkedHashMap<>();
            if (stem)
                path = path + "\\stemmed";
            FileReader fr = new FileReader(path + "\\docs.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            String s = "";
            while (line != null){
                String[] docDetails = line.split("~");
                String docID = docDetails[0];
                int maxTF = Integer.parseInt(docDetails[1]);
                int uniqueTerms = Integer.parseInt(docDetails[2]);
                String date = docDetails[3];
                String city = docDetails[4];
                String language = docDetails[5];
                int size = Integer.parseInt(docDetails[6]);
                Document d = new Document(docID,maxTF,uniqueTerms,date,city,language,size);
                documents.put(docID, d);
                line = br.readLine();
            }
            fr.close();
        }catch (Exception e) { e.printStackTrace(); }
    }

    private void loadDictionary(String path, boolean stem){
        try {
            dictionary = new LinkedHashMap<>();
            if (stem)
                path = path + "\\stemmed";
            FileReader fr = new FileReader(path + "\\dictionary.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            String s = "";
            while (line != null){
                String[] termDetails = line.split("#");
                String term = termDetails[0];
                int termFreq = Integer.parseInt(termDetails[1]);
                int docFreq = Integer.parseInt(termDetails[2]);
                int postingLine = Integer.parseInt(termDetails[3]);
                dictionary.put(term, new Term(term, termFreq, docFreq, postingLine));
                line = br.readLine();
            }
            fr.close();
        }catch (Exception e) { e.printStackTrace(); }
    }

    /**
     *
     * @return a list of the languages of the files
     */
    public LinkedHashSet<String> getLanguage(){
        return readFile.language;
    }

    /**
     *
     * @param stem - is stemmed or not
     * @return the dictionary of the search engine
     */
    public String getDictionary(boolean stem){
        List<String> sorted = new ArrayList<>();
        sorted.addAll(dictionary.keySet());
        Collections.sort(sorted, new SearchEngine.SortIgnoreCase());
        String s = "";
        for (int i = 0; i < sorted.size(); i++){
            String term = sorted.get(i);
            Term t = dictionary.get(term);
            s = s + term + ", TF: "+ t.termFreq + "\n";
        }
        return s;
    }

    /**
     * start the parsing and indexing process
     * @return the total time of the proceess (in seconds)
     */
    public double start() {
        long StartTime = System.nanoTime();
        dictionary = readFile.read();
        try {
            FileUtils.deleteDirectory(new File("C:\\TempFiles"));
        }catch (Exception e) { e.printStackTrace(); }
        long EndTime = System.nanoTime();
        double totalTime = (EndTime - StartTime)/1000000000.0;
        return totalTime;
    }

    /**
     *
     * @return the total number of unique terms in the corpus
     */
    public int getNumOfTerms(){
        return readFile.numOfTerms;
    }

    /**
     *
     * @return the total number of documents in the corpus
     */
    public int getNumOfDocs(){
        return readFile.numOfDocs;
    }

    /**
     * delete all index files
     */
    public void Reset() {
        File mainFolder = new File(postingPath);
        File[] folders = mainFolder.listFiles();
        for (File f : folders) {
            try {
                if (f.isDirectory())
                    FileUtils.deleteDirectory(f);
                else
                    Files.deleteIfExists(f.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public void RunMultipleQueries(String queryFilePath) {
        searcher.runQueries(dictionary, documents, queryFilePath, stem);
    }

    public void RunSingleQuery(String query) {
        searcher.runQuery(dictionary, documents, query, stem);
    }

    /**
     * comparator for sorting the dictionary
     */
    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

    public class SortByTF implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            int tf1 = ((Term)o1).termFreq;
            int tf2 = ((Term)o2).termFreq;
            return tf1-tf2;
        }
    }
}
