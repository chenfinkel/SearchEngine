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
import java.text.DecimalFormat;
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

    public static LinkedHashMap<String, String> cities = new LinkedHashMap<>();
    ;


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
    public void Index(HashMap<String, LinkedHashSet<Integer>> docTerms, String DocID, String city, String date, String language) {
        if (docTerms != null) {
            m.lock();
            if (!city.equals("X"))
                cities.put(city, "");
            m.unlock();
            int maxTF = 0;
            Iterator it = docTerms.keySet().iterator();
            while (it.hasNext()) {
                String termString = (String) it.next();
                LinkedHashSet<Integer> locations = docTerms.get(termString);
                int docTermFreq = locations.size();
                if (docTermFreq > maxTF)
                    maxTF = docTermFreq;
                insert(termString, docTermFreq, DocID, locations);
            }
            docs.add(DocID + "~" + maxTF + "~" + docTerms.size() + "~" + date + "~" + city + "~" + language);
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
                        postingEntry = postingEntry + "!" + freqs.get(j);
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
                for (int i = 0; i < sorted.size(); i++) {
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
    private void insert(String termString, int docTermFreq, String DocID, LinkedHashSet<Integer> locations) {
        if (termString.equals("")) return;
        String line = DocID + "*" + docTermFreq + ":";
        Iterator it = locations.iterator();
        while (it.hasNext()) {
            Integer loc = (Integer) it.next();
            if (it.hasNext())
                line = line + loc + ",";
            else
                line = line + loc;
        }
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
        Term t = new Term(termString);
        terms.put(termString, t);
        termsDocs.put(termString, new ArrayList<String>());
        termsDocs.get(termString).add(line);
    }


    //TAKES TOO MUCH TIME (OVER 5 MINUTES) AND WE DONT HANDLE DUPLICATES
    public void Merge() {
        mergeDirectory("posting");
        mergeDirectory("docs");
        indexCities();
    }

    private void mergeDirectory(String dir) {
        try {
            int index2 = 0;
            File folders = new File("D:\\searchEngine\\" + dir);
            File[] files = folders.listFiles();
            int size = files.length;
            if (size == 1)
                return;
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
            if (dir.equals("posting"))
                splitLetters(index2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeDocs(File left, File right, int TmpIndex) {
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
                    String[] details1 = split1[1].split("#");
                    String[] details2 = split2[1].split("#");
                    int tf = Integer.parseInt(details1[0]) + Integer.parseInt(details2[0]);
                    newToken = rightToken;
                    if (Character.isLetter(leftToken.charAt(0))) {
                        if (rightToken.charAt(0) < leftToken.charAt(0))
                            newToken = leftToken;
                    }
                    newLine = newToken + "~" + tf + "#" + details1[1] + details2[1];
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
                if (cities.containsKey(newToken.toUpperCase()))
                    cities.put(newToken.toUpperCase(), newLine);
            }
            if (leftLine != null) {
                newToken = leftLine.split("~")[0];
                if (cities.containsKey(newToken.toUpperCase()))
                    cities.put(newToken.toUpperCase(), leftLine);
                bw.write(leftLine);
                bw.newLine();
                while ((leftLine = brLeft.readLine()) != null) {
                    newToken = leftLine.split("~")[0];
                    if (cities.containsKey(newToken.toUpperCase()))
                        cities.put(newToken.toUpperCase(), leftLine);
                    bw.write(leftLine);
                    bw.newLine();
                }
            }
            if (rightLine != null) {
                newToken = rightLine.split("~")[0];
                if (cities.containsKey(newToken.toUpperCase()))
                    cities.put(newToken.toUpperCase(), rightLine);
                bw.write(rightLine);
                bw.newLine();
                while ((rightLine = brRight.readLine()) != null) {
                    newToken = rightLine.split("~")[0];
                    if (cities.containsKey(newToken.toUpperCase()))
                        cities.put(newToken.toUpperCase(), rightLine);
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

    private void indexCities(){
        try {
            FileWriter fw = new FileWriter("D:\\searchEngine\\cities\\cities.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            Iterator it = cities.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                String details = getDetails(entry.getKey());
                String posting = entry.getValue();
                String[] docs = posting.split("!");
                for (int i = 1; i < docs.length; i++) {
                    details = details + "!" + docs[i];
                }
                bw.write(details);
                bw.newLine();
                bw.flush();
            }
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
}