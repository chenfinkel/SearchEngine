package sample;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.security.spec.ECField;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SearchEngine {

    private String corpusPath;
    private String postingPath;
    private boolean stem;
    private ReadFile readFile;
    public LinkedHashMap<String, Term> dictionary;

    public SearchEngine() {
        new File("posting").mkdirs();
        new File("docs").mkdirs();
        new File("cities").mkdirs();
    }

    public void setProps(String cp, String pp, boolean stem) {
        corpusPath = cp;
        postingPath = pp;
        this.stem = stem;
        readFile = new ReadFile(cp, pp, stem);

    }

    public String getPostingPath() {
        return postingPath;
    }

    public void loadDict(String path){
        try {
            FileInputStream fis = new FileInputStream(path + "\\dictionary.txt");
            ObjectInputStream oos = new ObjectInputStream(fis);
            dictionary = (LinkedHashMap<String, Term>)oos.readObject();
        }catch (Exception e){ e.printStackTrace(); }
    }

    public LinkedHashSet<String> getLanguage(){
        return readFile.language;
    }

    public String getDictionary(){
        List<String> sorted = new ArrayList<>();
        sorted.addAll(dictionary.keySet());
        Collections.sort(sorted, new SearchEngine.SortIgnoreCase());
        String s = "";
        for (int i = 0; i < sorted.size(); i++){
            String term = sorted.get(i);
            Term t = dictionary.get(term);
            s = s + term + ": TF: "+ t.termFreq + "\n";
        }
        return s;
    }

    public double start() {
        long StartTime = System.nanoTime();
        dictionary = readFile.read();
        long EndTime = System.nanoTime();
        double totalTime = (EndTime - StartTime)/1000000000.0;
        return totalTime;
    }

    public int getNumOfTerms(){
        return readFile.numOfTerms;
    }

    public int getNumOfDocs(){
        return readFile.numOfDocs;
    }

    public void Reset() {
        File mainFolder = new File(postingPath);
        File[] folders = mainFolder.listFiles();
        for (File f : folders) {
            try {
                Files.deleteIfExists(f.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }
}
