package sample;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import sun.awt.Mutex;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.nio.file.Files;
import java.nio.file.Paths;
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

    private LinkedHashSet<String> docs;


    public Indexer() {
        dictionary = new LinkedHashMap<>();
        terms = new LinkedHashMap<>();
        termsDocs = new LinkedHashMap<>();
        docs = new LinkedHashSet<>();
        m.lock();
        index++;
        fileIndex = index;
        m.unlock();

    }

    //string- the term, int- df in - docid- docNo
    public void Index(HashMap<String, Integer> docTerms, String DocID, String city, String date, String language) {
        if (docTerms != null) {
            int maxTF = 0;
            Iterator it = docTerms.keySet().iterator();
            while (it.hasNext()) {
                String termString = (String) it.next();
                int docTermFreq = docTerms.get(termString);
                if (docTermFreq > maxTF)
                    maxTF = docTermFreq;
                insert(termString, docTermFreq, DocID);
            }
            docs.add(DocID+"~" + maxTF + "~" + docTerms.size() + "~" + date + "~" + city + "~" + language);
        } else {
            try {
                FileWriter fw = new FileWriter("D:\\searchEngine\\posting\\" + fileIndex + ".txt");
                BufferedWriter bw = new BufferedWriter(fw);
                List<String> sorted = new ArrayList<>();
                sorted.addAll(terms.keySet());
                Collections.sort(sorted, new SortIgnoreCase());
                for (int i = 0; i < sorted.size(); i++) {
                    String key = sorted.get(i);
                    String postingEntry = "";
                    postingEntry = key + "~" + terms.get(key).docFreq + "#";
                    ArrayList<String> freqs = new ArrayList<>();
                    freqs.addAll(termsDocs.get(key));
                    for (int j = 0; j < freqs.size(); j++) {
                        postingEntry = postingEntry + "|" + freqs.get(j);
                    }
                    postingEntry = postingEntry + System.lineSeparator();
                    bw.write(postingEntry);
                    bw.flush();
                }
                fw.close();
                fw = new FileWriter("D:\\searchEngine\\docs\\file" + fileIndex + ".txt");
                bw = new BufferedWriter(fw);
                sorted = new ArrayList<>();
                sorted.addAll(docs);
                Collections.sort(sorted, new SortIgnoreCase());
                for (int i = 0; i < sorted.size(); i++){
                    String s = sorted.get(i);
                    bw.write(s);
                    bw.newLine();
                    bw.flush();
                }
                fw.close();
                termsDocs = new LinkedHashMap<>();
                terms = new LinkedHashMap<>();
            } catch (Exception e) {
                e.printStackTrace();
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
        mergeDirectory("posting");
        mergeDirectory("docs");
    }

    private void mergeDirectory(String dir) {
        try {
            int index2 = 0;
            File folders = new File("D:\\searchEngine\\" + dir);
            File[] files = folders.listFiles();
            int size = files.length;
            while (size != 1) {
                int i;
                for (i = 0; i < size - 1; i = i + 2) {
                    index2++;
                    File f1 = files[i];
                    File f2 = files[i + 1];
                    if (dir.equals("posting"))
                        mergePosting(f1, f2, index2);
                    else
                        mergeDocs(f1, f2, index2);
                }
                files = folders.listFiles();
                size = files.length;
            }
            FileReader fr = new FileReader("D:\\searchEngine\\"+ dir + "\\tmp" + index2 + ".txt");
            BufferedReader br = new BufferedReader(fr);
            int counter = 0;
            while (br.readLine() != null)
                counter++;
            System.out.println(dir + ": " + counter);
            fr.close();
            if (dir.equals("posting"))
                splitLetters(index2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeDocs(File left, File right, int TmpIndex){
        try {
            FileWriter fw = new FileWriter("D:\\searchEngine\\docs\\tmp" + TmpIndex + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            FileReader frLeft = new FileReader(left.getPath());
            BufferedReader brLeft = new BufferedReader(frLeft);
            FileReader frRight = new FileReader(right.getPath());
            BufferedReader brRight = new BufferedReader(frRight);
            int leftIdx = 0;
            int rightIdx = 0;
            String newLine = "";
            String leftLine = brLeft.readLine();
            String rightLine = brRight.readLine();
            while (leftLine != null && rightLine != null) {
                if (leftLine.equals("") || rightLine.equals(""))
                    continue;
                String[] split1 = leftLine.split("~");
                String[] split2 = rightLine.split("~");
                String leftToken = split1[0];
                String rightToken = split2[0];
                if (leftToken.compareToIgnoreCase(rightToken) < 0) {
                    bw.write(leftLine);
                    bw.newLine();
                    leftLine = brLeft.readLine();
                } else {
                    bw.write(rightLine);
                    bw.newLine();
                    rightLine = brRight.readLine();
                }
            }
            if (leftLine != null) {
                bw.write(leftLine);
                bw.newLine();
                while ((leftLine = brLeft.readLine()) != null) {
                    bw.write(leftLine);
                    bw.newLine();
                }
            }
            if (rightLine != null) {
                bw.write(rightLine);
                bw.newLine();
                while ((rightLine = brRight.readLine()) != null) {
                    bw.write(rightLine);
                    bw.newLine();
                }
            }
            bw.flush();
            fw.close();
            frLeft.close();
            frRight.close();
            Files.deleteIfExists(left.toPath());
            Files.deleteIfExists(right.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergePosting(File left, File right, int TmpIndex) {
        try {
            FileWriter fw = new FileWriter("D:\\searchEngine\\posting\\tmp" + TmpIndex + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            FileReader frLeft = new FileReader(left.getPath());
            BufferedReader brLeft = new BufferedReader(frLeft);
            FileReader frRight = new FileReader(right.getPath());
            BufferedReader brRight = new BufferedReader(frRight);
            int leftIdx = 0;
            int rightIdx = 0;
            String newLine = "";
            String leftLine = brLeft.readLine();
            String rightLine = brRight.readLine();
            while (leftLine != null && rightLine != null) {
                if (leftLine.equals("") || rightLine.equals(""))
                    continue;
                String[] split1 = leftLine.split("~");
                String[] split2 = rightLine.split("~");
                String leftToken = split1[0];
                String rightToken = split2[0];
                if (leftToken.equalsIgnoreCase(rightToken)) {
                    String[] details1 = split1[1].split("#");
                    String[] details2 = split2[1].split("#");
                    int tf = Integer.parseInt(details1[0]) + Integer.parseInt(details2[0]);
                    String newToken = rightToken;
                    if (Character.isLetter(leftToken.charAt(0))) {
                        if (rightToken.charAt(0) < leftToken.charAt(0))
                            newToken = leftToken;
                    }
                    bw.write(newToken + "~" + tf + "#" + details1[1] + details2[1]);
                    bw.newLine();
                    leftLine = brLeft.readLine();
                    rightLine = brRight.readLine();

                } else if (leftToken.compareToIgnoreCase(rightToken) < 0) {
                    bw.write(leftLine);
                    bw.newLine();
                    leftLine = brLeft.readLine();
                } else {
                    bw.write(rightLine);
                    bw.newLine();
                    rightLine = brRight.readLine();
                }
            }
            if (leftLine != null) {
                bw.write(leftLine);
                bw.newLine();
                while ((leftLine = brLeft.readLine()) != null) {
                    bw.write(leftLine);
                    bw.newLine();
                }
            }
            if (rightLine != null) {
                bw.write(rightLine);
                bw.newLine();
                while ((rightLine = brRight.readLine()) != null) {
                    bw.write(rightLine);
                    bw.newLine();
                }
            }
            bw.flush();
            fw.close();
            frLeft.close();
            frRight.close();
            Files.deleteIfExists(left.toPath());
            Files.deleteIfExists(right.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void splitLetters(int pIndex) {
        try {
            FileReader fr = new FileReader("D:\\searchEngine\\posting\\tmp" + pIndex + ".txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            char first = line.charAt(0);
            while (line != null) {
                char tmp = first;
                String name = (first + "").toLowerCase();
                FileWriter fw = new FileWriter("D:\\searchEngine\\posting\\" + name + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                while (first == tmp) {
                    bw.write(line);
                    bw.newLine();
                    line = br.readLine();
                    if (line == null)
                        break;
                    first = line.charAt(0);
                }
                bw.flush();
                fw.close();
            }
            fr.close();
            Files.deleteIfExists(Paths.get("D:\\searchEngine\\posting\\tmp" + pIndex + ".txt"));

        } catch (Exception e) {
            e.printStackTrace();
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