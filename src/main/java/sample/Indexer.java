package sample;

import sun.awt.Mutex;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** this class takes a list of parsed terms, and creates an index containing posting files and dictionary */
public class Indexer {

    /** the id of the indexer */
    public static int index = 0;

    /** mutex for indexer's id */
    public static Mutex m = new Mutex();

    /** mutex for number of documents */
    public static Mutex numOfDocsMutex = new Mutex();

    /** mutex for number of terms */
    public static Mutex numOfTermsMutex = new Mutex();

    /** the id of the file */
    private int fileIndex;

    /** number of total documents */
    public static int numOfDocs;

    /** number of total unique terms */
    public static int numOfTerms;

    /** maps a term to the documents it appeared in */
    private LinkedHashMap<String, ArrayList<String>> termsDocs;

    /** maps a term to it's object */
    private LinkedHashMap<String, Term> terms;

    /** a list of documents */
    private LinkedHashSet<String> docs;

    /** the dictionary for the terms */
    public LinkedHashMap<String, Term> dictionary;

    /** a list of the cities of the documents */
    private LinkedHashSet<String> cities;

    /** a list of the languages of the file */
    private LinkedHashSet<String> languages;

    /** the location of the index */
    private String postPath;

    /** a final list the languages */
    public LinkedHashSet<String> FinalLanguage;

    /** empty constructor */
    public Indexer() {
        terms = new LinkedHashMap<>();
        termsDocs = new LinkedHashMap<>();
        docs = new LinkedHashSet<>();
        cities = new LinkedHashSet<>();
        languages = new LinkedHashSet<>();
        m.lock();
        index++;
        fileIndex = index;
        m.unlock();
    }

    /**
     * this method saves the terms and documents, until the parser tell it to index a temporary posting file
     * @param docTerms the terms of a document
     * @param DocID the document id
     * @param city the city of the document
     * @param date the date of the creation of the document
     * @param language the language of the document
     */
    public void Index(HashMap<String, Integer> docTerms, String DocID, String city, String date, String language) {
        if (docTerms != null) {
            if (!city.equals("X"))
                cities.add(city);
            if (!language.equals("X"))
                languages.add(language);
            int maxTF = 0;
            Iterator it = docTerms.keySet().iterator();
            while (it.hasNext()) {
                String termString = (String) it.next();
                int docTermFreq = docTerms.get(termString);
                if (docTermFreq > maxTF)
                    maxTF = docTermFreq;
                insert(termString, docTermFreq, DocID);
            }
            docs.add(DocID + "~" + maxTF + "~" + docTerms.size() + "~" + date + "~" + city + "~" + language);
            numOfDocsMutex.lock();
            numOfDocs++;
            numOfDocsMutex.unlock();
        } else {
            try {
                FileWriter fw = new FileWriter("C:\\posting\\" + fileIndex + ".txt");
                BufferedWriter bw = new BufferedWriter(fw);
                List<String> sorted = new ArrayList<>();
                sorted.addAll(terms.keySet());
                Collections.sort(sorted, new SortIgnoreCase());
                for (int i = 0; i < sorted.size(); i++) {
                    String key = sorted.get(i);
                    String postingEntry = "";
                    postingEntry = key + "~" + terms.get(key).termFreq + "~" + terms.get(key).docFreq + "#";
                    ArrayList<String> freqs = new ArrayList<>();
                    freqs.addAll(termsDocs.get(key));
                    for (int j = 0; j < freqs.size(); j++) {
                        postingEntry = postingEntry + "!" + freqs.get(j);
                    }
                    postingEntry = postingEntry + System.lineSeparator();
                    bw.write(postingEntry);
                }
                bw.flush();
                fw.close();

                writeTempFile("C:\\docs\\file" + fileIndex + ".txt", docs);

                writeTempFile("C:\\city\\file" + fileIndex + ".txt", cities);

                writeTempFile("C:\\languages\\file" + fileIndex + ".txt", languages);

                terms = new LinkedHashMap<>();
                termsDocs = new LinkedHashMap<>();
                docs = new LinkedHashSet<>();
                cities = new LinkedHashSet<>();
                languages = new LinkedHashSet<>();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void insert(String termString, int docTermFreq, String DocID) {
        if (termString.equals("")) return;
        String line = DocID + "*" + docTermFreq;
        if (Character.isUpperCase(termString.charAt(0))) {
            String lowerCase = termString.toLowerCase();
            if (terms.containsKey(lowerCase)) {
                Term t = terms.get(lowerCase);
                t.increaseDF();
                t.termFreq = t.termFreq + docTermFreq;
                termsDocs.get(lowerCase).add(line);
                return;
            } else {
                termString = termString.toUpperCase();
            }
        } else {
            termString = termString.toLowerCase();
            String upperCase = termString.toUpperCase();
            if (terms.containsKey(upperCase))
                terms.remove(upperCase);
        }
        Term t = null;
        if (terms.containsKey(termString))
            t = terms.get(termString);
        else {
            t = new Term(termString);
            terms.put(termString, t);
            termsDocs.put(termString, new ArrayList<String>());
        }
        t.increaseDF();
        t.termFreq = t.termFreq + docTermFreq;
        termsDocs.get(termString).add(line);
    }

    private void writeTempFile(String path, Collection<String> c) {
        try {
            FileWriter fw = new FileWriter(path);
            BufferedWriter bw = new BufferedWriter(fw);
            ArrayList<String> sorted = new ArrayList<>();
            sorted.addAll(c);
            Collections.sort(sorted, new SortIgnoreCase());
            for (int i = 0; i < sorted.size(); i++) {
                String s = sorted.get(i);
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * merge all temorary posting files
     * @param path the location of the index
     * @param stem if stemming was done
     */
    public void Merge(String path, boolean stem) {
        if (stem) {
            postPath = path + "\\stemmed";
            new File(postPath).mkdirs();
        } else
            postPath = path;
        ExecutorService exeServ = Executors.newFixedThreadPool(3);
        exeServ.submit(new mergeThread("docs", postPath, this));
        exeServ.submit(new mergeThread("city", postPath, this));
        exeServ.submit(new mergeThread("languages", postPath, this));
        exeServ.shutdown();
        try {
            exeServ.awaitTermination(30, TimeUnit.MINUTES);
        } catch (Exception e) { e.printStackTrace(); }
        mergeDirectory();
        saveDictionary();
    }

    /** sort class for sorting string collections ignore case */
    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

    private void mergeDirectory() {
        try {
            int index2 = 0;
            File folders = new File("C:\\posting");
            File[] files = folders.listFiles();
            int size = files.length;
            while (size > 1) {
                int i;
                for (i = 0; i < size - 1; i = i + 2) {
                    index2++;
                    File f1 = files[i];
                    File f2 = files[i + 1];
                    mergePosting(f1, f2, index2);
                }
                files = folders.listFiles();
                size = files.length;
            }
            String path = "";
            if (index2 != 0)
                path = "C:\\posting\\tmp" + index2 + ".txt";
            else
                path = "C:\\posting\\1.txt";
            createDictionary(path);
            splitLetters(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergePosting(File left, File right, int TmpIndex) {
        try {
            FileWriter fw = new FileWriter("C:\\posting\\tmp" + TmpIndex + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            FileReader frLeft = new FileReader(left.getPath());
            BufferedReader brLeft = new BufferedReader(frLeft);
            FileReader frRight = new FileReader(right.getPath());
            BufferedReader brRight = new BufferedReader(frRight);
            int leftIdx = 0;
            int rightIdx = 0;
            String newLine = "", newToken = "";
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
                    String LeftTF = split1[1];
                    String RightTF = split2[1];
                    String[] details1 = split1[2].split("#");
                    String[] details2 = split2[2].split("#");
                    String LeftDF = details1[0];
                    String RightDF = details2[0];
                    int DF = Integer.parseInt(LeftDF) + Integer.parseInt(RightDF);
                    int tf = Integer.parseInt(LeftTF) + Integer.parseInt(RightTF);
                    newToken = rightToken;
                    if (Character.isLetter(leftToken.charAt(0))) {
                        if (rightToken.charAt(0) < leftToken.charAt(0))
                            newToken = leftToken.toLowerCase();
                    }
                    newLine = newToken + "~" + tf + "~" + DF + "#" + details1[1] + details2[1];
                    leftLine = brLeft.readLine();
                    rightLine = brRight.readLine();
                } else if (leftToken.compareToIgnoreCase(rightToken) < 0) {
                    newLine = leftLine;
                    leftLine = brLeft.readLine();
                } else {
                    newLine = rightLine;
                    rightLine = brRight.readLine();
                }
                bw.write(newLine);
                bw.newLine();
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

    private void splitLetters(String path) {
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            char first = line.charAt(0);
            while (line != null) {
                char tmp = first;
                String name = (first + "").toLowerCase();
                FileWriter fw = new FileWriter(postPath + "\\" + name + ".txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                int lineNum = 0;
                while (Character.toLowerCase(first) == Character.toLowerCase(tmp)) {
                    lineNum++;
                    String term = line.split("~")[0];
                    bw.write(line);
                    bw.newLine();
                    dictionary.get(term).postingLine = lineNum;
                    line = br.readLine();
                    if (line == null)
                        break;
                    first = line.charAt(0);
                }
                bw.flush();
                fw.close();
            }
            fr.close();
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDictionary(String path) {
        try {
            dictionary = new LinkedHashMap<>();
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                numOfTermsMutex.lock();
                numOfTerms++;
                numOfTermsMutex.unlock();
                String[] split = line.split("~");
                String term = split[0];
                Term t = new Term(term);
                t.termFreq = Integer.parseInt(split[1]);
                t.docFreq = Integer.parseInt(split[2].split("#")[0]);
                dictionary.put(term, t);
                line = br.readLine();
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDictionary(){
        try {
            FileWriter fw = new FileWriter(postPath + "\\dictionary.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            FileWriter fw1 = new FileWriter(postPath + "\\TMPdictionary.txt", true);
            BufferedWriter bw1 = new BufferedWriter(fw1);
            List<String> sorted = new ArrayList<>();
            sorted.addAll(dictionary.keySet());
            Collections.sort(sorted, new Indexer.SortIgnoreCase());
            for (int i = 0; i < sorted.size(); i++){
                String term = sorted.get(i);
                Term t = dictionary.get(term);
                bw.write(term + "#"+ t.termFreq + "#" + t.docFreq + "#"+ t.postingLine);
                bw1.write(t.termFreq);
                bw.newLine();
                bw1.newLine();
            }
            bw.flush();
            bw1.flush();
            fw1.close();
            fw.close();
        }catch (Exception e) { e.printStackTrace(); }
    }

}