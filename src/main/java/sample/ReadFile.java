package sample;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class ReadFile {

    //private Parse parser;
    private String path;
    private String postPath;
    private boolean stem;
    private ExecutorService exeServ;
    public LinkedHashSet<String> language;
    public int numOfTerms;
    public int numOfDocs;


    public ReadFile(){
        path = "";
    }

    public ReadFile(String path, String postPath, boolean stem) {
        this.path = path;
        this.postPath = postPath;
        this.stem = stem;
        exeServ = Executors.newFixedThreadPool(10);
    }

    /**
     * Read all file and separate them into documents
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
