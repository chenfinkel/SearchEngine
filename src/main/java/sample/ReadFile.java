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


    /** thread pool */
    private ExecutorService exeServ;


    /** empty constructor */
    public ReadFile(){
        exeServ = Executors.newFixedThreadPool(10);
    }

    /**
     * Read all files, separate them into documents and send it to parser
     * @return the dictionary of the search engine
     */
    public void read(){
        File mainFolder = new File(SearchEngine.corpusPath);
        File[] folders = mainFolder.listFiles();
        long StartTime = System.nanoTime();
            for (File folder : folders) {
            if(!folder.getName().equals("stop_words.txt"))
                exeServ.submit(new ReadThread(folder));
        }
        exeServ.shutdown();
        try {
            exeServ.awaitTermination(30, TimeUnit.MINUTES);
        } catch (Exception e) { e.printStackTrace(); }
        long EndTime = System.nanoTime();
        double totalTime = (EndTime - StartTime) / 1000000000.0;
        System.out.println("finish parse, time: " + totalTime);
        Indexer indexer = new Indexer();
        StartTime = System.nanoTime();
        indexer.Merge();
        EndTime = System.nanoTime();
        totalTime = (EndTime - StartTime) / 1000000000.0;
        System.out.println("finish index, time: " + totalTime);
    }
}
