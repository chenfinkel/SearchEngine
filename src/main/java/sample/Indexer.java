package sample;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.util.*;

public class Indexer {

    private int index = 0;
    // Dictionary maps terms to their posting file
    private LinkedHashMap<Term, Posting> dictionary;
    //terms maps a term to it's object
    private LinkedHashMap<String, Term> terms;
    //termsDocs maps a term to the documents it appeared in
    private LinkedHashMap<String, ArrayList<String>> termsDocs;


    public Indexer(){
        dictionary = new LinkedHashMap<Term, Posting>();
        terms = new LinkedHashMap<String, Term>();
        termsDocs = new LinkedHashMap<String, ArrayList<String>>();
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
                index++;
                try {
                    FileWriter fw = new FileWriter("C:\\Users\\yarinab\\IdeaProjects\\Posting" + index + ".txt");
                    BufferedWriter bw = new BufferedWriter(fw);
                    TreeMap<String, Term> sorted = new TreeMap<>();
                    sorted.putAll(terms);
                    // Hello~3:|D1-3|D2-4|D3-5|/n
                    for (Map.Entry<String, Term> entry : sorted.entrySet()) {
                        String key = entry.getKey();
                        String postingEntry = "";
                        postingEntry = postingEntry + key + "~" + entry.getValue().docFreq;
                        ArrayList<String> freqs = new ArrayList<>();
                        freqs.addAll(termsDocs.get(key));
                        for (int i = 0; i < freqs.size(); i++) {
                            postingEntry = postingEntry + "|" + freqs.get(i);
                        }
                        postingEntry = postingEntry + System.lineSeparator();
                        bw.write(postingEntry);
                        bw.flush();
                    }
                    termsDocs = new LinkedHashMap<String, ArrayList<String>>();
                    terms = new LinkedHashMap<String, Term>();
                } catch (Exception e) { }
            }
        }
    }
    // CHECK UPPERCASE CODE
    private void insert(String termString, int docTermFreq, String DocID) {
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
            FileWriter SFW = new FileWriter("C:\\Users\\yarinab\\IdeaProjects\\S.txt");
            BufferedWriter SBW = new BufferedWriter(SFW);
            FileReader SFR = new FileReader("C:\\Users\\yarinab\\IdeaProjects\\S.txt");
            BufferedReader SBR = new BufferedReader(SFR);
            String currLine = "", currLineS="";
            TreeSet<String> LineS = new TreeSet<>();
            int idx = 0;
            for(int i = 1; i <= index; i++) {
                FileReader PFR = new FileReader("C:\\Users\\yarinab\\IdeaProjects\\Posting" + i +".txt");
                BufferedReader PBR = new BufferedReader(PFR);
                while((currLineS = SBR.readLine()) != null)
                    LineS.add(currLineS + System.lineSeparator());
                while((currLine = PBR.readLine()) != null) {
                    if(currLine.charAt(0) == 's' || currLine.charAt(0) == 'S') {
                        LineS.add(currLine + System.lineSeparator());
                    }
                }
                Iterator it = LineS.iterator();
                String toWrite = "";
                while(it.hasNext())
                    toWrite = toWrite + it.next();
                SBW.write(toWrite);
                SBW.flush();
            }
        }catch (Exception e) {}
    }
}
