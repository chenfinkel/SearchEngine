package sample;

import javafx.util.Pair;
import org.apache.commons.io.FileUtils;
import org.decimal4j.util.DoubleRounder;
import sun.awt.Mutex;

import java.io.*;
import java.lang.management.GarbageCollectorMXBean;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.spec.ECField;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Indexer {

    public static int index = 0;

    public static Mutex m = new Mutex();

    public static Mutex numOfDocsMutex = new Mutex();

    public static Mutex numOfTermsMutex = new Mutex();

    private int fileIndex;

    public static int numOfDocs;

    public static int numOfTerms;

    //termsDocs maps a term to the documents it appeared in
    private LinkedHashMap<String, ArrayList<String>> termsDocs;

    private LinkedHashMap<String, Term> terms;

    private LinkedHashSet<String> docs;

    public LinkedHashMap<String, Term> dictionary;

    private LinkedHashSet<String> cities;

    private LinkedHashSet<String> languages;

    private String postPath;


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

    //string- the term, int- df in - docid- docNo
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

                writeTempFile("C:\\cities\\file" + fileIndex + ".txt", cities);

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

    private void writeTempFile(String path, Collection<String> c){
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
        }catch (Exception e) { e.printStackTrace(); }
    }


    // CHECK UPPERCASE CODE
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


    //TAKES TOO MUCH TIME (OVER 5 MINUTES) AND WE DONT HANDLE DUPLICATES
    public void Merge(String path, boolean stem) {
        if(stem) {
            postPath = path + "\\stemmed";
            new File(postPath).mkdirs();
        }else
            postPath = path;
        mergeDirectory("posting");
        mergeDirectory("docs");
        mergeDirectory("cities");
        mergeDirectory("languages");
        //indexCities();
    }

    private void mergeDirectory(String dir) {
        try {
            int index2 = 0;
            File folders = new File("C:\\" + dir);
            File[] files = folders.listFiles();
            int size = files.length;
            if (size == 1 && (dir.equals("docs") || dir.equals("language") || dir.equals("cities"))) {
                FileUtils.copyFile(files[0], new File(postPath + "\\"+ dir+".txt"));
                Files.deleteIfExists(files[0].toPath());
            }
            while (size > 1) {
                int i;
                for (i = 0; i < size - 1; i = i + 2) {
                    index2++;
                    File f1 = files[i];
                    File f2 = files[i + 1];
                    if (dir.equals("posting"))
                        mergePosting(f1, f2, index2);
                    else {
                        if(size == 2) {
                            mergeFiles(f1, f2, index2, true, dir);
                            break;
                        }
                        else
                            mergeFiles(f1, f2, index2, false, dir);
                    }
                }
                files = folders.listFiles();
                size = files.length;
            }

            if (dir.equals("posting")) {
                String path = "";
                if (index2 != 0)
                    path = "C:\\posting\\tmp" + index2 + ".txt";
                else
                    path = "C:\\posting\\1.txt";
                createDictionary(path);
                splitLetters(path);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeFiles(File left, File right, int TmpIndex, boolean flag, String dir) {
        try {
            FileWriter fw;
            if(flag)
                fw = new FileWriter(postPath + "\\" + dir +".txt");
            else
                fw = new FileWriter("C:\\"+dir+"\\tmp" + TmpIndex + ".txt");
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
                String leftToken = leftLine;
                String rightToken = rightLine;
                if (dir.equals("docs")) {
                    String[] split1 = leftLine.split("~");
                    String[] split2 = rightLine.split("~");
                    leftToken = split1[0];
                    rightToken = split2[0];
                }
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
                            newToken = leftToken;
                    }
                    newLine = newToken + "~" + tf + "~" + DF + "#" + details1[1] + details2[1];
                    leftLine = brLeft.readLine();
                    rightLine = brRight.readLine();

                } else if (leftToken.compareToIgnoreCase(rightToken) < 0) {
                    newLine = leftLine;
                    newToken = leftToken;
                    leftLine = brLeft.readLine();
                } else {
                    newLine = rightLine;
                    newToken = rightToken;
                    rightLine = brRight.readLine();
                }
                bw.write(newLine);
                bw.newLine();
            }
            if (leftLine != null) {
                newToken = leftLine.split("~")[0];
                bw.write(leftLine);
                bw.newLine();
                while ((leftLine = brLeft.readLine()) != null) {
                    newToken = leftLine.split("~")[0];
                    bw.write(leftLine);
                    bw.newLine();
                }
            }
            if (rightLine != null) {
                newToken = rightLine.split("~")[0];
                bw.write(rightLine);
                bw.newLine();
                while ((rightLine = brRight.readLine()) != null) {
                    newToken = rightLine.split("~")[0];
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

    public class SortIgnoreCase implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            String s1 = (String) o1;
            String s2 = (String) o2;
            return s1.toLowerCase().compareTo(s2.toLowerCase());
        }
    }

    private void indexCities(){
        try {
            FileWriter fw = new FileWriter(postPath + "\\cities.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            Iterator it = cities.iterator();
            while (it.hasNext()) {
                String details = getDetails((String)it.next());
                bw.write(details);
                bw.newLine();
            }
            bw.flush();
            fw.close();
        }catch (Exception e) { e.printStackTrace(); }
    }

    private String getDetails(String city) {
        try {
            URL url = null;
            String ans = "";
            url = new URL(" http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + city);
            if (url != null) {

                URLConnection urlConnection = url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));

                String line = "", country = "", population = "", currency = "";

                while ((line = br.readLine()) != null) {
                    String[] tmp = line.split("\"");
                    line = "";
                    for (String s : tmp)
                        line = line + s;

                    if (line.contains("geobytescurrencycode"))
                        currency = getValue(line, "geobytescurrencycode");

                    if (line.contains("geobytespopulation")) {
                        population = getValue(line, "geobytespopulation");
                        if (!population.equals("X"))
                            population = getNumber(population);
                    }

                    if (line.contains("geobytescountry"))
                        country = getValue(line, "geobytescountry");
                }
                ans = city + "," + country + "," + population + "," + currency;
                return ans;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getValue(String line, String detail) {
        String[] splitedLine = line.split(",");
        String s = "";
        for (int i = 0; i < splitedLine.length; i++) {
            if (splitedLine[i].contains(detail)) {
                s = splitedLine[i];
                break;
            }
        }
        String[] tmp = s.split(":");
        if (tmp.length > 1)
            return tmp[1];
        return "X";
    }

    private String getNumber(String s){
        double num = Double.parseDouble(s);
        String Snum = "";
        if (num >= 1000000000){
            num = num/1000000000;
            num = DoubleRounder.round(num, 2);
            if (num == (int)num)
                Snum = (int)num + "B";
            else
                Snum = num + "B";
        } else if(num >= 1000000){
            num = num/1000000;
            num = DoubleRounder.round(num, 2);
            if (num == (int)num)
                Snum = (int)num + "M";
            else
                Snum = num + "M";
        } else if (num >= 1000){
            num = num/1000;
            num = DoubleRounder.round(num, 2);
            if (num == (int)num)
                Snum = (int)num + "K";
            else
                Snum = num + "K";
        } else
            Snum = s;
        return Snum;
    }

    private void createDictionary(String path){
        try {
            dictionary = new LinkedHashMap<>();
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String line = br.readLine();
            while (line != null){
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
            File file = new File(postPath + "\\dictionary.txt");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(postPath + "\\dictionary.txt");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(dictionary);
            oos.flush();
            fos.close();
        }catch(Exception e) { e.printStackTrace(); }
    }

}