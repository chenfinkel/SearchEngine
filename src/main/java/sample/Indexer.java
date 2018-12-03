package sample;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import sun.awt.Mutex;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.nio.file.Files;
import java.util.*;

public class Indexer {

    public static int index = 0;

    public static Mutex m = new Mutex();

    private int fileIndex;

    // Dictionary maps terms to their posting file
    private LinkedHashMap<Term, Posting> dictionary;
    //terms maps a term to it's object
    private LinkedHashMap<String, Term> terms;
    //termsDocs maps a term to the documents it appeared in
    private LinkedHashMap<String, ArrayList<String>> termsDocs;


    public Indexer(){
        dictionary = new LinkedHashMap<>();
        terms = new LinkedHashMap<>();
        termsDocs = new LinkedHashMap<>();
        m.lock();
        index++;
        fileIndex = index;
        m.unlock();

    }

    //string- the term, int- df in - docid- docNo
    public void Index(HashMap<String, Integer> docTerms, String DocID) {
        if (DocID.equals("done")) {
            Merge();
        } else {
            if (docTerms != null) {
                Iterator it = docTerms.keySet().iterator();
                while (it.hasNext()) {
                    String termString = (String) it.next();
                    int docTermFreq = docTerms.get(termString);
                    insert(termString, docTermFreq, DocID);
                }
            } else {
                try {
                    FileWriter fw = new FileWriter("D:\\searchEngine\\posting\\"+ fileIndex + ".txt");
                    BufferedWriter bw = new BufferedWriter(fw);
                    TreeMap<String, Term> sorted = new TreeMap<>();
                    sorted.putAll(terms);
                    for (Map.Entry<String, Term> entry : sorted.entrySet()) {
                        String key = entry.getKey();
                        String postingEntry = "";
                        postingEntry = postingEntry + key + "~" + entry.getValue().docFreq + "#";
                        ArrayList<String> freqs = new ArrayList<>();
                        freqs.addAll(termsDocs.get(key));
                        for (int i = 0; i < freqs.size(); i++) {
                            postingEntry = postingEntry + "|" + freqs.get(i);
                        }
                        postingEntry = postingEntry + System.lineSeparator();
                        bw.write(postingEntry);
                        bw.flush();
                        //bw.close();
                    }
                    fw.close();
                    termsDocs = new LinkedHashMap<>();
                    terms = new LinkedHashMap<>();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }
    }
    // CHECK UPPERCASE CODE
    private void insert(String termString, int docTermFreq, String DocID) {
        if (termString.equals("")) return;
        if (Character.isUpperCase(termString.charAt(0))) {
            String lowerCase = termString.toLowerCase();
            if (terms.containsKey(lowerCase)) {
                Term t = terms.get(lowerCase);
                t.increaseDF();
                t.termFreq = t.termFreq + docTermFreq;
                termsDocs.get(lowerCase).add(DocID + "*" + docTermFreq);
                return;
            } else {
                termString = termString.toUpperCase();
            }
        } else {
            String upperCase = termString.toUpperCase();
            if (terms.containsKey(upperCase)) {
                terms.remove(upperCase);
            }
        }
        Term t = new Term(termString);
        terms.put(termString, t);
        termsDocs.put(termString, new ArrayList<String>());
        termsDocs.get(termString).add(DocID + "*" + docTermFreq);
    }

    //TAKES TOO MUCH TIME (OVER 5 MINUTES) AND WE DONT HANDLE DUPLICATES
    public void Merge() {
        try {
            int index = 0;
            FileWriter fw;
            BufferedWriter bw;
            BufferedReader br;
            FileReader fr;
            File postings = new File("D:\\searchEngine\\posting");
            File[] folders = postings.listFiles();
            int size = folders.length;
            while (size != 1) {
                int i;
                for (i = 0; i < size - 1; i = i+2) {
                    index++;
                    File f1 = folders[i];
                    File f2 = folders[i+1];
                    fr = new FileReader(f1.getPath());
                    br = new BufferedReader(fr);
                    String line, file11 = "";
                    while ((line= br.readLine()) != null) {
                        file11 = file11 + line;
                    }
                    fr.close();
                    fr = new FileReader(f2.getPath());
                    br = new BufferedReader(fr);
                    String file22 = "";
                    while ((line= br.readLine()) != null) {
                        file22 = file22 + line;
                    }
                    fr.close();
                    String[] file1 = file11.split(System.lineSeparator());
                    String[] file2 = file22.split(System.lineSeparator());
                    //String[] file1 = FileUtils.readFileToString(f1).split(System.lineSeparator());
                    //String[] file2 = FileUtils.readFileToString(f2).split(System.lineSeparator());
                    try {
                        Files.deleteIfExists(f1.toPath());
                        Files.deleteIfExists(f2.toPath());
                    } catch (Exception e) { e.printStackTrace();}
                    String merged = merge(file1, file2);
                    fw = new FileWriter("D:\\searchEngine\\posting\\tmp"+ index + ".txt");
                    bw = new BufferedWriter(fw);
                    bw.write(merged);
                    bw.flush();
                    fw.close();
                }
                if (i == size -1){
                    File f1 = folders[i];
                    File f2 = new File("D:\\searchEngine\\posting\\tmp"+ index + ".txt");
                    fr = new FileReader(f1.getPath());
                    br = new BufferedReader(fr);
                    String line, file11 = "";
                    while ((line= br.readLine()) != null) {
                        file11 = file11 + line;
                    }
                    fr.close();
                    fr = new FileReader(f2.getPath());
                    br = new BufferedReader(fr);
                    String file22 = "";
                    while ((line= br.readLine()) != null) {
                        file22 = file22 + line;
                    }
                    fr.close();
                    String[] file1 = file11.split(System.lineSeparator());
                    String[] file2 = file22.split(System.lineSeparator());
                    //String[] file1 = FileUtils.readFileToString(f1).split(System.lineSeparator());
                    //String[] file2 = FileUtils.readFileToString(f2).split(System.lineSeparator());
                    try {
                        Files.deleteIfExists(f1.toPath());
                        Files.deleteIfExists(f2.toPath());
                    } catch (Exception e) { e.printStackTrace();}
                    String merged = merge(file1, file2);
                    //index++;
                    fw = new FileWriter("D:\\searchEngine\\posting\\tmp"+ index + ".txt");
                    bw = new BufferedWriter(fw);
                    bw.write(merged);
                    bw.flush();
                    fw.close();
                }
                folders = postings.listFiles();
                size = folders.length;
            }

        }catch (Exception e) {}
    }


    private String merge(String[] left, String[] right) {
        String merged = "";
        int leftIdx = 0;
        int rightIdx = 0;
        String newLine = "";
        while (leftIdx < left.length && rightIdx < right.length){
            String[] split1 = left[leftIdx].split("~");
            String[] split2 = right[rightIdx].split("~");
            String leftToken = split1[0];
            String rightToken = split2[0];
            if (leftToken.equalsIgnoreCase(rightToken)){
                String[] details1 = split1[1].split("#");
                String[] details2 = split2[1].split("#");
                int tf = Integer.parseInt(details1[0])+ Integer.parseInt(details2[0]);
                String newToken = rightToken;
                if (Character.isLetter(leftToken.charAt(0))){
                    if (rightToken.charAt(0) < leftToken.charAt(0))
                        newToken = leftToken;
                }
                merged = merged + newToken + "~" + tf + "#" + details1[1] + details2[1] + System.lineSeparator();
                leftIdx++;
                rightIdx++;
            }
            else if (leftToken.compareTo(rightToken) < 0) {
                merged = merged + left[leftIdx] + System.lineSeparator();
                leftIdx++;
            }
            else{
                merged = merged + right[rightIdx] + System.lineSeparator();
                rightIdx++;
            }
        }
        if (leftIdx < left.length) {
            for (int i = leftIdx; i < left.length; i++)
                merged = merged + left[i] + System.lineSeparator();
        }
        if (rightIdx < right.length) {
            for (int i = rightIdx; i < right.length; i++)
                merged = merged + right[i] + System.lineSeparator();
        }
        return merged;
    }
}
