package sample;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * this class reads a document corpus
 */
public class ReadFile {

    /** the location of the corpus */
    private String path;

    /** the path where the index files will be saved */
    private String postPath;

    /** indicates if the parsing process is done with stemming or not */
    private boolean stem;

    /** thread pool */
    private ExecutorService exeServ;

    /** a list of the languages of the files */
    public LinkedHashSet<String> language;

    /** total number of unique terms in the corpus */
    public int numOfTerms;

    /** total number of documents in the corpus */
    public int numOfDocs;


    /** empty constructor */
    public ReadFile(){
        path = "";
    }

    /** constructor */
    public ReadFile(String path, String postPath, boolean stem) {
        this.path = path;
        this.postPath = postPath;
        this.stem = stem;
        exeServ = Executors.newFixedThreadPool(10);
    }

    /**
     * Read all files, separate them into documents and send it to parser
     * @return the dictionary of the search engine
     */
    public LinkedHashMap<String, Term> read(){
        File mainFolder = new File(path);
        File[] folders = mainFolder.listFiles();
            for (File folder : folders) {
            if(!folder.getName().equals("stop_words.txt"))
                exeServ.submit(new ParseThread(folder, path, postPath, stem));
        }
        exeServ.shutdown();
        try {
            exeServ.awaitTermination(30, TimeUnit.MINUTES);
        } catch (Exception e) { e.printStackTrace(); }
        System.out.println("finish parse");
        Indexer indexer = new Indexer();
        indexer.Merge(postPath, stem);
        System.out.println("finish index");
        language = indexer.FinalLanguage;
        numOfDocs = Indexer.numOfDocs;
        numOfTerms = Indexer.numOfTerms;
        return indexer.dictionary;

    }

}
