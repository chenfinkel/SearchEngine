package sample;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.decimal4j.util.DoubleRounder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * This class represents a search engine
 */

public class SearchEngine {

    /**
     * the location of the corpus
     */
    public static String corpusPath;

    /**
     * the path where the index files will be saved
     */
    public static String postingPath;

    /**
     * indicates if the process is done with stemming or not
     */
    public static boolean stem;

    /**
     * reads the corpus
     */
    private ReadFile readFile;

    /**
     * the dictionary of the search engine after prasing and indexing
     */
    public static ConcurrentHashMap<String, Term> dictionary;

    public static ConcurrentHashMap<String, Document> documents;

    public static ConcurrentHashMap<String, String> languages;

    public static ConcurrentHashMap<String, City> cities;
    /**
     * the searcher of the engine
     */
    private Searcher searcher;

    /**
     * constructor
     */
    public SearchEngine() {
        new File("C:\\TempFiles").mkdirs();
        new File("C:\\TempFiles\\posting").mkdirs();
        new File("C:\\TempFiles\\docs").mkdirs();
        new File("C:\\TempFiles\\city").mkdirs();
        new File("C:\\TempFiles\\languages").mkdirs();
        new File("C:\\TempFiles\\DocTF").mkdirs();
        documents = new ConcurrentHashMap<>();
        dictionary = new ConcurrentHashMap<>();
        languages = new ConcurrentHashMap<>();
        cities = new ConcurrentHashMap<>();
        Indexer.index = 0;
    }

    /**
     * set the properties for the search engine
     *
     * @param cp   - the location of the corpus
     * @param pp   - the location for the index to be saved
     * @param stem - indicates if the parse will we with stemming or not
     */
    public void setProps(String cp, String pp, boolean stem) {
        corpusPath = cp;
        postingPath = pp;
        SearchEngine.stem = stem;
        readFile = new ReadFile();
    }

    /**
     * get the location of the index
     */
    public String getPostingPath() {
        return postingPath;
    }

    /**
     * loads the dictionary from the disk to the main memory
     *
     * @param path - the location of the dictionary
     * @param stem - is stemmed or not
     */
    public boolean loadDict(String path, boolean stem) {
        try {
            loadDictionary(path, stem);
            loadDocs(path, stem);
            loadTFIDF(path, stem);
            loadLanguages(path, stem);
            loadCities(path, stem);
            searcher = new Searcher();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void loadLanguages(String path, boolean stem) {
        try {
            FileReader fr = new FileReader(path + "\\languages.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                languages.put(line, line);
                line = br.readLine();
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCities(String path, boolean stem) {
        try {
            FileReader fr = new FileReader(path + "\\cities.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                String[] details = line.split("#");
                String city = details[0];
                String state = details[1];
                String population = details[2];
                String currency = details[3];
                cities.put(city, new City(city, state, population, currency));
                City c = cities.get(city);
                String[] docs = details[4].split("~");
                for (int i = 0; i < docs.length; i++)
                    c.addDocument(documents.get(docs[i]));
                line = br.readLine();
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadTFIDF(String path, boolean stem) {
        try {
            if (stem)
                path = path + "\\stemmed";
            FileReader fr = new FileReader(path + "\\tfidf.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null) {
                String[] docDetails = line.split("~");
                String docID = docDetails[0];
                double tfidf = Double.parseDouble(docDetails[1]);
                documents.get(docID).setSumOfSquareTFIDF(tfidf);
                line = br.readLine();
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDocs(String path, boolean stem) {
        try {
            if (stem)
                path = path + "\\stemmed";
            FileReader fr = new FileReader(path + "\\docs.txt");
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            String s = "";
            while (line != null) {
                String[] docDetails = line.split("~");
                String docID = docDetails[0];
                int maxTF = Integer.parseInt(docDetails[1]);
                int uniqueTerms = Integer.parseInt(docDetails[2]);
                String date = docDetails[3];
                String city = docDetails[4];
                String language = docDetails[5];
                int size = Integer.parseInt(docDetails[6]);
                Document d = new Document(docID, maxTF, uniqueTerms, date, city, language, size);
                if (docDetails.length == 8) {
                    String[] entity = docDetails[7].split("#");
                    List<Pair<String, Double>> entities = new ArrayList<>();
                    for (int i = 0; i < entity.length; i++) {
                        String[] tmp = entity[i].split("\\*");
                        Double Rank = Double.parseDouble(tmp[1]);
                        String term = tmp[0];
                        entities.add(new Pair(term, Rank));
                    }
                    d.setPrimaryEntities(entities);
                }
                documents.put(docID, d);
                line = br.readLine();
            }
            fr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadDictionary(String path, boolean stem) throws Exception {
        dictionary = new ConcurrentHashMap<>();
        if (stem)
            path = path + "\\stemmed";
        FileReader fr = new FileReader(path + "\\dictionary.txt");
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        String s = "";
        while (line != null) {
            String[] termDetails = line.split("#");
            String term = termDetails[0];
            int termFreq = Integer.parseInt(termDetails[1]);
            int docFreq = Integer.parseInt(termDetails[2]);
            int postingLine = Integer.parseInt(termDetails[3]);
            dictionary.put(term, new Term(term, termFreq, docFreq, postingLine));
            line = br.readLine();
        }
        fr.close();
    }

    /**
     * @return a list of the languages of the files
     */
    public ConcurrentHashMap<String, String> getLanguage() {
        return languages;
    }

    /**
     * @return the dictionary of the search engine
     */
    public List<Term> getDictionary() {
        List<String> sorted = new ArrayList<>();
        sorted.addAll(dictionary.keySet());
        Collections.sort(sorted, new SearchEngine.SortIgnoreCase());
        String s = "";
        List<Term> entries = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            String term = sorted.get(i);
            Term t = dictionary.get(term);
            entries.add(t);
        }
        return entries;
    }

    /**
     * start the parsing and indexing process
     *
     * @return the total time of the proceess (in seconds)
     */
    public double start() {
        long StartTime = System.nanoTime();
        readFile.read();
        save();
        try {
            FileUtils.deleteDirectory(new File("C:\\TempFiles"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        long EndTime = System.nanoTime();
        double totalTime = (EndTime - StartTime) / 1000000000.0;
        searcher = new Searcher();
        return totalTime;
    }

    private void save() {
        try {
            Thread t1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String path = postingPath;
                        if (stem)
                            path = path + "\\stemmed";
                        FileWriter fw = new FileWriter(path + "\\dictionary.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        List<String> sorted = new ArrayList<>();
                        sorted.addAll(SearchEngine.dictionary.keySet());
                        Collections.sort(sorted, new SortIgnoreCase());
                        for (int i = 0; i < sorted.size(); i++) {
                            String term = sorted.get(i);
                            Term t = SearchEngine.dictionary.get(term);
                            String s = term + "#" + t.termFreq + "#" + t.docFreq + "#" + t.postingLine;
                            bw.write(s);
                            bw.newLine();
                        }
                        bw.flush();
                        fw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread t2 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String path = postingPath;
                        if (stem)
                            path = path + "\\stemmed";
                        FileWriter fw = new FileWriter(path + "\\cities.txt", true);
                        BufferedWriter bw = new BufferedWriter(fw);
                        List<String> sorted = new ArrayList<>();
                        sorted.addAll(SearchEngine.cities.keySet());
                        Collections.sort(sorted, new SortIgnoreCase());
                        for (int i = 0; i < sorted.size(); i++) {
                            String city = sorted.get(i);
                            City c = SearchEngine.cities.get(city);
                            String s = city + "#" + c.getState() + "#" + c.getPopulation() + "#" + c.getCurrency() + "#";
                            LinkedHashSet<Document> docs = c.getDocuments();
                            Iterator<Document> it = docs.iterator();
                            while (it.hasNext()) {
                                Document doc = it.next();
                                if (it.hasNext())
                                    s = s + doc.getDocID() + "~";
                                else
                                    s = s + doc.getDocID();
                            }
                            bw.write(s);
                            bw.newLine();
                        }
                        bw.flush();
                        fw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            Thread t3 = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String path = postingPath;
                        if (stem)
                            path = path + "\\stemmed";
                        BufferedWriter bw = new BufferedWriter(new FileWriter(path + "\\tfidf.txt"));
                        Iterator<Document> it = documents.values().iterator();
                        while (it.hasNext()) {
                            Document d = it.next();
                            String s = d.getDocID() + "~" + d.getSumOfSquareTFIDF();
                            bw.write(s);
                            bw.newLine();
                        }
                        bw.flush();
                        bw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            t1.start();
            t2.start();
            t3.start();
            t1.join();
            t2.join();
            t3.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveTfIdf() {
        try {
            String path = postingPath;
            if (stem)
                path = path + "\\stemmed";
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + "\\tfidf.txt"));
            Iterator<Document> it = documents.values().iterator();
            while (it.hasNext()) {
                Document d = it.next();
                bw.write(d.getDocID() + "~" + d.getSumOfSquareTFIDF());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the total number of unique terms in the corpus
     */
    public int getNumOfTerms() {
        return dictionary.size();
    }

    /**
     * @return the total number of documents in the corpus
     */
    public int getNumOfDocs() {
        return documents.size();
    }

    /**
     * delete all index files
     */
    public void Reset() {
        File mainFolder = new File(postingPath);
        File[] folders = mainFolder.listFiles();
        for (File f : folders) {
            try {
                if (f.isDirectory())
                    FileUtils.deleteDirectory(f);
                else
                    Files.deleteIfExists(f.toPath());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public List<QueryResult> RunMultipleQueries(String queryFilePath, boolean semantic, HashSet<String> cities) {
        searcher.setSemantic(semantic);
        searcher.setCities(cities);
        searcher.runQueries(queryFilePath);
        List<QueryResult> results = searcher.getResults();
        return results;
    }

    public void saveResults(String path) {
        resultsToFile(path, searcher.getResults());
    }

    public List<QueryResult> RunSingleQuery(String query, boolean semantic, HashSet<String> cities) {
        searcher.setSemantic(semantic);
        searcher.setCities(cities);
        searcher.runQuery(query);
        return searcher.getResults();
    }

    private void resultsToFile(String path, List<QueryResult> results) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(path + "\\results.txt"));
            for (int i = 0; i < results.size(); i++) {
                QueryResult qr = results.get(i);
                String qrNum = qr.getQueryNumber();
                List<Map.Entry<Document, Double>> docs = qr.getDocuments();
                for (int j = 0; j < docs.size(); j++)
                    bw.write(qrNum + " 0 " + docs.get(j).getKey().getDocID() + " 1 42.38 mt" + System.lineSeparator());
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveDictionary() {
        try {
            String path = postingPath;
            if (stem)
                path = path + "\\stemmed";
            FileWriter fw = new FileWriter(path + "\\dictionary.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            List<String> sorted = new ArrayList<>();
            sorted.addAll(SearchEngine.dictionary.keySet());
            Collections.sort(sorted, new SortIgnoreCase());
            for (int i = 0; i < sorted.size(); i++) {
                String term = sorted.get(i);
                Term t = SearchEngine.dictionary.get(term);
                bw.write(term + "#" + t.termFreq + "#" + t.docFreq + "#" + t.postingLine);
                bw.newLine();
            }
            bw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveCities() {
        try {
            String path = postingPath;
            if (stem)
                path = path + "\\stemmed";
            FileWriter fw = new FileWriter(path + "\\cities.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            List<String> sorted = new ArrayList<>();
            sorted.addAll(SearchEngine.cities.keySet());
            Collections.sort(sorted, new SortIgnoreCase());
            for (int i = 0; i < sorted.size(); i++) {
                String city = sorted.get(i);
                City c = SearchEngine.cities.get(city);
                String s = city + "," + c.getState() + "," + c.getPopulation() + "," + c.getCurrency() + ",";
                LinkedHashSet<Document> docs = c.getDocuments();
                Iterator<Document> it = docs.iterator();
                while (it.hasNext()) {
                    Document doc = it.next();
                    if (it.hasNext())
                        s = s + doc.getDocID() + "~";
                    else
                        s = s + doc.getDocID();
                }
                bw.write(s);
                bw.newLine();
            }
            bw.flush();
            fw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<Pair<String, Double>> getEntities(String docID) {
        return documents.get(docID).getPrimaryEntities();
    }

    private void setCitiesDocs() {
        String path = postingPath;
        if (stem)
            path = path + "\\stemmed";
        Iterator<City> it = cities.values().iterator();
        while (it.hasNext()) {
            City c = it.next();
            String city = c.getCity();
            Term t = null;
            if (dictionary.contains(city))
                t = dictionary.get(city);
            else if (dictionary.contains(city.toLowerCase()))
                t = dictionary.get(city.toLowerCase());
            if (t != null) {
                try {
                    Stream<String> lines = Files.lines(Paths.get(path + "\\" + Character.toLowerCase(city.charAt(0)) + ".txt"));
                    String line = lines.skip(t.postingLine - 1).findFirst().get();
                    String[] split = line.split("#!");
                    String[] split2 = split[1].split("!");
                    for (int i = 0; i < split2.length; i++) {
                        String[] split3 = split2[i].split("\\*");
                        c.addDocument(documents.get(split3[0]));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * comparator for sorting the dictionary
     */
    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }
}
