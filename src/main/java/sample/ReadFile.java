package sample;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    private String textToParse;
    private String docID;


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
    public void read(){
        long StartTime = System.nanoTime();
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

        long EndTime = System.nanoTime();
        double totalTime = (EndTime - StartTime)/1000000.0;
        System.out.println("Parse time:  " + totalTime/60000.0 + " min");
        StartTime = System.nanoTime();
        Indexer indexer = new Indexer();
        indexer.Merge(postPath, stem);
        EndTime = System.nanoTime();
        totalTime = (EndTime - StartTime)/1000000.0;
        System.out.println("Index time:  " + totalTime/60000.0 + " min");

    }

}
