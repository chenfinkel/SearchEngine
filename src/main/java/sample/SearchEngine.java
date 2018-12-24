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
        calcTFIDF();
        try {
            FileUtils.deleteDirectory(new File("C:\\TempFiles"));
        }catch (Exception e) { e.printStackTrace(); }
        long EndTime = System.nanoTime();
        double totalTime = (EndTime - StartTime)/1000000000.0;
        return totalTime;
    }

    private void calcTFIDF() {
        int numOfDocs = readFile.numOfDocs;
        try {
            FileWriter fw = new FileWriter(postingPath+"\\TFIDF.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            FileReader fr = new FileReader("C:\\TempFiles\\DocTF\\docTF.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while(line != null){
                String[] split = line.split(":");
                //PUT A BREAKPOINT WHEN SPLIT LENGTH == 1
                String docno = split[0];
                bw.write(docno+":");
                String[] docTerms = split[1].split("~");
                for (int i = 0; i < docTerms.length; i++){
                    if (!docTerms[i].equals("")) {
                        String[] split2 = docTerms[i].split("\\*");
                        String term = split2[0];
                        String tfstr = split2[1];
                        Term t = null;
                        if (dictionary.containsKey(term))
                            t = dictionary.get(term);
                        else if (dictionary.containsKey(term.toLowerCase()))
                            t = dictionary.get(term.toLowerCase());
                        else
                            t = dictionary.get(term.toUpperCase());
                        if (t==null)
                            continue;
                        int tf = Integer.parseInt(tfstr);
                        double idf = Math.log(numOfDocs / t.docFreq);
                        double tfidf = tf * idf;
                        bw.write(term + "*" + DoubleRounder.round(tfidf, 4) + "~");
                    }
                }
                bw.newLine();
                line = br.readLine();
            }
            bw.flush();
            fw.close();
            fr.close();
        }catch (Exception e) { e.printStackTrace(); }
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
        searcher.runQueries(queryFilePath);
    }

    public void RunSingleQuery(String query) {
        searcher.runQuery(query);
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
