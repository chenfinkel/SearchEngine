package sample;

import javafx.util.Pair;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.decimal4j.util.DoubleRounder;
import sun.awt.Mutex;

import javax.swing.text.html.HTMLDocument;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * this class takes a list of parsed terms, and creates an index containing posting files and dictionary
 */
public class Indexer {

    /** the id of the indexer*/
    public static int index = 0;

    /** mutex for indexer's id*/
    public static Mutex m = new Mutex();


    /**
     * the id of the file
     */
    private int fileIndex;

    /**
     * maps a term to the documents it appeared in
     */
    private LinkedHashMap<String, LinkedHashMap<String,Integer>> termsDocs;

    /**
     * maps a term to it's object
     */
    private LinkedHashMap<String, Term> terms;

    /**
     * a list of documents
     */
    private LinkedHashSet<String> docs;

    /**
     * a list of the languages of the file
     */
    private LinkedHashSet<String> languages;

    /**
     * the location of the index
     */
    private String postPath;

    /**
     * empty constructor
     */
    public Indexer() {
        terms = new LinkedHashMap<>();
        termsDocs = new LinkedHashMap<>();
        docs = new LinkedHashSet<>();
        languages = new LinkedHashSet<>();
        m.lock();
        index++;
        fileIndex = index;
        m.unlock();
    }

    /**
     * this method saves the terms and documents, until the parser tell it to index a temporary posting file
     */
    public void Index() {
        try {
            FileWriter fw = new FileWriter("C:\\TempFiles\\posting\\" + fileIndex + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            List<String> sorted = new ArrayList<>();
            sorted.addAll(terms.keySet());
            Collections.sort(sorted, new SortIgnoreCase());
            for (int i = 0; i < sorted.size(); i++) {
                String key = sorted.get(i);
                String postingEntry = "";
                int docFreq = termsDocs.get(key).size();
                terms.get(key).docFreq = docFreq;
                postingEntry = key + "~" + terms.get(key).termFreq + "~" + docFreq + "#";
                LinkedHashMap<String, Integer> docs = termsDocs.get(key);
                Iterator<Map.Entry<String,Integer>> it = docs.entrySet().iterator();
                while(it.hasNext()){
                    Map.Entry<String,Integer> entry = it.next();
                    postingEntry = postingEntry + "!" + entry.getKey() + "*" + entry.getValue();
                }
                postingEntry = postingEntry + System.lineSeparator();
                bw.write(postingEntry);
            }
            bw.flush();
            fw.close();

            writeTempFile("C:\\TempFiles\\docs\\file" + fileIndex + ".txt", docs);

            writeTempFile("C:\\TempFiles\\languages\\file" + fileIndex + ".txt", languages);

            terms = new LinkedHashMap<>();
            termsDocs = new LinkedHashMap<>();
            docs = new LinkedHashSet<>();
            languages = new LinkedHashSet<>();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveDetails(HashMap<String, Integer> docTerms, Document doc) {
        String DocID = doc.getDocID();
        String city = doc.getCity();
        String language = doc.getLanguage();
        if (!language.equals("X"))
            languages.add(language);
        int maxTF = 0, docSize = 0;
        Iterator it = docTerms.keySet().iterator();
        while (it.hasNext()) {
            String termString = (String) it.next();
            int docTermFreq = docTerms.get(termString);
            docSize = docSize + docTermFreq;
            if (docTermFreq > maxTF)
                maxTF = docTermFreq;
            insert(termString, docTermFreq, DocID);
        }
        SearchEngine.documents.put(DocID, new Document(DocID,maxTF,docTerms.size(),doc.getDate(),city,language,docSize));
        Document d = SearchEngine.documents.get(DocID);
        String s = DocID + "~" + maxTF + "~" + docTerms.size() + "~" + doc.getDate() + "~" + city + "~" + language + "~" + docSize + "~";
        List<Pair<String, Double>> primaryEntities = getPrimaryEntities(docTerms, maxTF);
        if (primaryEntities != null) {
            d.setPrimaryEntities(primaryEntities);
            int i;
            for (i = 0; i < primaryEntities.size() - 1; i++) {
                s = s + primaryEntities.get(i).getKey() + "*" + primaryEntities.get(i).getValue() + "#";
            }
            s = s + primaryEntities.get(i).getKey() + "*" + primaryEntities.get(i).getValue();
        }
        docs.add(s);
        if (!city.equals("X")) {
            City c;
            if (!SearchEngine.cities.containsKey(city)){
                c = new City(city);
                SearchEngine.cities.put(city, c);
                c.setDetailsFromAPI("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=");
            } else
                c = SearchEngine.cities.get(city);
            c.addDocument(d);
        }
    }

    private void insert(String termString, int docTermFreq, String DocID) {
        if (termString.equals("")) return;
        String line = DocID + "*" + docTermFreq;
        if (Character.isUpperCase(termString.charAt(0))) {
            String lowerCase = termString.toLowerCase();
            if (terms.containsKey(lowerCase)) {
                Term t = terms.get(lowerCase);
                t.termFreq = t.termFreq + docTermFreq;
                if (termsDocs.get(lowerCase).containsKey(DocID)){
                    int doctf = termsDocs.get(lowerCase).get(DocID);
                    termsDocs.get(lowerCase).put(DocID, docTermFreq+doctf);
                }else {
                    termsDocs.get(lowerCase).put(DocID, docTermFreq);
                }
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
            termsDocs.put(termString, new LinkedHashMap<>());
        }
        t.termFreq = t.termFreq + docTermFreq;
        termsDocs.get(termString).put(DocID, docTermFreq);
    }

    private LinkedHashMap<String,Integer> findEntities(HashMap<String, Integer> docTerms){
        LinkedHashMap<String, Integer> entities = new LinkedHashMap<>();
        Iterator<Map.Entry<String, Integer>> it = docTerms.entrySet().iterator();
        while(it.hasNext()){
            Map.Entry<String, Integer> entry = it.next();
            String termString = entry.getKey();
            if (!termString.equals("")) {
                int frequency = entry.getValue();
                if (Character.isUpperCase(termString.charAt(0)))
                    entities.put(termString.toUpperCase(), frequency);
                else {
                    if (entities.containsKey(termString.toUpperCase()))
                        entities.remove(termString.toUpperCase());
                }
            }
        }
        return entities;
    }

    private List<Pair<String, Double>> getPrimaryEntities(HashMap<String, Integer> docTerms, int maxTF){
        LinkedHashMap<String, Integer> entities = findEntities(docTerms);
        List<Pair<String, Double>> primaryEntities = null;
        if (entities.size() > 0) {
            primaryEntities = new ArrayList<>();
            List<Pair<String, Double>> grades = new ArrayList<>();
            Iterator<Map.Entry<String, Integer>> it = entities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> entry = it.next();
                String term = entry.getKey();
                int frequency = entry.getValue();
                double grade = ((double) frequency) / maxTF;
                grades.add(new Pair<>(term, grade));
            }
            Collections.sort(grades, new sortByGrade());
            for (int i = 0; i < 5 && i < grades.size(); i++)
                primaryEntities.add(grades.get(i));
        }
        return primaryEntities;
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
     *
     */
    public void Merge() {
        if (SearchEngine.stem) {
            postPath = SearchEngine.postingPath + "\\stemmed";
            new File(postPath).mkdirs();
        } else
            postPath = SearchEngine.postingPath;
        ExecutorService exeServ = Executors.newFixedThreadPool(3);
        exeServ.submit(new mergeThread("docs", postPath, this));
        exeServ.submit(new mergeThread("languages", postPath, this));
        exeServ.shutdown();
        try {
            exeServ.awaitTermination(30, TimeUnit.MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mergeDirectory();
    }

    /**
     * sort class for sorting string collections ignore case
     */
    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

    public class sortByGrade implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            Pair<Document, Double> s1 = (Pair<Document, Double>) o1;
            Pair<Document, Double> s2 = (Pair<Document, Double>) o2;
            double res = s1.getValue() - s2.getValue();
            if (res < 0)
                return 1;
            else if (res > 0)
                return -1;
            else
                return 0;
        }
    }

    private void mergeDirectory() {
        try {
            int index2 = 0;
            File folders = new File("C:\\TempFiles\\posting");
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
                path = "C:\\TempFiles\\posting\\tmp" + index2 + ".txt";
            else
                path = "C:\\TempFiles\\posting\\1.txt";
            createDictionary(path);
            splitLetters(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergePosting(File left, File right, int TmpIndex) {
        try {
            FileWriter fw = new FileWriter("C:\\TempFiles\\posting\\tmp" + TmpIndex + ".txt");
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
                    String[] details = line.split("~");
                    String term = details[0];
                    bw.write(line);
                    bw.newLine();
                    SearchEngine.dictionary.get(term).postingLine = lineNum;
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
            SearchEngine.dictionary = new ConcurrentHashMap<>();
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                String[] splitt = line.split("~");
                String term = splitt[0];
                City c = SearchEngine.cities.get(term);
                Term t = new Term(term);
                t.termFreq = Integer.parseInt(splitt[1]);
                t.docFreq = Integer.parseInt(splitt[2].split("#")[0]);
                SearchEngine.dictionary.put(term, t);

                String[] split = line.split("#!");
                String[] split2 = split[1].split("!");
                for (int i = 0; i < split2.length; i++) {
                    String[] split3 = split2[i].split("\\*");
                    String docID = split3[0];
                    Integer tfDoc = Integer.parseInt(split3[1]);
                    double idf = Math.log(SearchEngine.documents.size() / t.docFreq);
                    double tfidf = tfDoc * idf;
                    Document d = SearchEngine.documents.get(docID);
                    d.addTFIDF(tfidf);
                    if (c != null)
                        c.addDocument(d);
                }

                line = br.readLine();
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}