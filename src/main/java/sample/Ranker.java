package sample;

import javax.swing.text.html.HTMLDocument;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Ranker {

    private double avdl;

    private boolean semantic;

    private HashSet<String> cities;

    public Ranker(double avdl, boolean semantic) {
        this.avdl = avdl;
        this.semantic = semantic;
        cities = new HashSet<>();
    }

    public List<Map.Entry<Document, Double>> Rank(String title) {
        try {
            Parse p = new Parse();
            LinkedHashMap<String, Integer> parsedQuery = p.parseQuery(title);
            if (semantic) {
                semanticTreatment(parsedQuery);
            }
            LinkedHashMap<Term, Integer> queryTerms = getQueryTerms(parsedQuery);
            LinkedHashMap<Term, LinkedHashMap<String, Integer>> termsDocs = getTermsDocs(queryTerms);
            HashMap<Document, Double> ranks = new HashMap<>();
            Iterator<Document> it;
            if (cities.size() == 0)
                it = SearchEngine.documents.values().iterator();
            else {
                LinkedHashSet<Document> cityDocs = getCityDocs();
                it = cityDocs.iterator();
            }
            while (it.hasNext()) {
                Document document = it.next();
                double rank = CalcDocRank(queryTerms, termsDocs, document);
                if (rank > 0)
                    ranks.put(document, rank);
            }
            List<Map.Entry<Document, Double>> sorted = new LinkedList<>(ranks.entrySet());
            Collections.sort(sorted, new Ranker.sort());
            List<Map.Entry<Document, Double>> result = new ArrayList<>();
            for (int i = 0; i < 50 && i < sorted.size(); i++) {
                Map.Entry<Document, Double> entry = sorted.get(i);
                result.add(entry);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private HashSet<String> getSemantics(LinkedHashMap<String, Integer> parsedQuery) {
        HashSet<String> semantics = new HashSet<>();
        Iterator<String> it = parsedQuery.keySet().iterator();
        while (it.hasNext()) {
            WordSemantics ws = new WordSemantics();
            HashSet<String> sem = ws.getSemanticsWords(it.next());
            Iterator<String> it2 = sem.iterator();
            while (it2.hasNext()) {
                String semanticWord = it2.next();
                semantics.add(semanticWord);
            }
        }
        return semantics;
    }

    private void semanticTreatment(LinkedHashMap<String, Integer> parsedQuery) {
        Parse p = new Parse();
        HashSet<String> semantics = getSemantics(parsedQuery);
        LinkedHashMap<String, Integer> parsedSemantics = new LinkedHashMap<>();
        Iterator<String> it = semantics.iterator();
        while (it.hasNext()) {
            LinkedHashMap<String, Integer> parsed = p.parseQuery(it.next());
            Iterator<Map.Entry<String, Integer>> it2 = parsed.entrySet().iterator();
            while (it2.hasNext()) {
                Map.Entry<String, Integer> entry = it2.next();
                if (parsedSemantics.containsKey(entry.getKey())) {
                    int freq = parsedSemantics.get(entry.getKey());
                    parsedSemantics.put(entry.getKey(), freq + entry.getValue());
                } else {
                    parsedSemantics.put(entry.getKey(), entry.getValue());
                }
            }
            parsedSemantics.putAll(parsed);
        }
        parsedQuery.putAll(parsedSemantics);
    }

    private LinkedHashMap<Term, Integer> getQueryTerms(LinkedHashMap<String, Integer> parsedQuery) {
        LinkedHashMap<Term, Integer> qeuryTerms = new LinkedHashMap<>();
        Iterator<Map.Entry<String, Integer>> it = parsedQuery.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            String term = entry.getKey();
            Term t = SearchEngine.dictionary.get(term.toLowerCase());
            if (t == null)
                t = SearchEngine.dictionary.get(term.toUpperCase());
            if (t != null)
                qeuryTerms.put(t, entry.getValue());
        }
        return qeuryTerms;
    }

    private LinkedHashMap<Term, LinkedHashMap<String, Integer>> getTermsDocs(LinkedHashMap<Term, Integer> queryTerms) {
        String path = SearchEngine.postingPath;
        if (SearchEngine.stem)
            path = path + "\\stemmed";
        LinkedHashMap<Term, LinkedHashMap<String, Integer>> postingLines = new LinkedHashMap<>();
        Iterator<Map.Entry<Term, Integer>> it = queryTerms.entrySet().iterator();
        while (it.hasNext()) {
            Term t = it.next().getKey();
            postingLines.put(t, new LinkedHashMap<>());
            String term = t.getId();
            try {
                Stream<String> lines = Files.lines(Paths.get(path + "\\" + Character.toLowerCase(term.charAt(0)) + ".txt"));
                String line = lines.skip(t.postingLine - 1).findFirst().get();
                String[] split = line.split("#!");
                String[] split2 = split[1].split("!");
                for (int i = 0; i < split2.length; i++) {
                    String[] split3 = split2[i].split("\\*");
                    Integer tfDoc = Integer.parseInt(split3[1]);
                    postingLines.get(t).put(split3[0], tfDoc);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return postingLines;
    }

    private double CalcDocRank(LinkedHashMap<Term, Integer> queryTerms, LinkedHashMap<Term, LinkedHashMap<String, Integer>> termsDocs, Document doc) {
        double ans = 0, bm25 = 0, cosSim = 0;
        String term = "";
        int tfQuery = 0, tfDoc;
        Iterator<Map.Entry<Term, LinkedHashMap<String, Integer>>> it = termsDocs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Term, LinkedHashMap<String, Integer>> entry = it.next();
            Term t = entry.getKey();
            LinkedHashMap<String, Integer> docs = termsDocs.get(t);
            try {
                String docID = doc.getDocID();
                if (docs.containsKey(docID)) {
                    tfDoc = docs.get(docID);
                    tfQuery = queryTerms.get(t);
                    bm25 = bm25 + BM25(t, doc, tfDoc, tfQuery);
                    cosSim = cosSim + cosSimilarity(t, doc, tfDoc, queryTerms.size());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        ans = 0.1 * bm25 + 0.9 * cosSim;
        return ans;
    }

    private double cosSimilarity(Term t, Document doc, int tfDoc, int numOfQueryTerms) {
        int numOfDocs = SearchEngine.documents.size();
        double numerator = 0;
        double normalTF = tfDoc / doc.getMaxTF();
        double idf = Math.log(numOfDocs / t.docFreq);
        numerator = numerator + (normalTF * idf);
        double denominator = Math.sqrt(numOfQueryTerms) * Math.sqrt(doc.getSumOfSquareTFIDF());
        return numerator / denominator;
    }

    private double BM25(Term t, Document doc, int tfDoc, int tfQuery) {
        double B = 0.35;
        double K = 1.2;
        int length = doc.getSize();
        int df = t.docFreq;
        int numOfDocs = SearchEngine.documents.size();
        double first = (K + 1) * tfDoc;
        double second = B * (length / avdl);
        double third = 1 - B + second;
        double fourth = K * third;
        double fifth = tfDoc + fourth;
        double log = Math.log(numOfDocs + 1 / df);
        double ans = tfQuery * first / fifth * log;
        return ans;
    }

    public void setCities(HashSet<String> cities) {
        this.cities.addAll(cities);
    }

    public LinkedHashSet<Document> getCityDocs() {
        Iterator<String> it = cities.iterator();
        LinkedHashSet<Document> docs = new LinkedHashSet<>();
        while (it.hasNext()) {
            String city = it.next();
            City c = SearchEngine.cities.get(city);
            docs.addAll(c.getDocuments());
        }
        return docs;
    }

    public class sort implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            Map.Entry<Document, Double> s1 = (Map.Entry<Document, Double>) o1;
            Map.Entry<Document, Double> s2 = (Map.Entry<Document, Double>) o2;
            double res = s1.getValue() - s2.getValue();
            if (res < 0)
                return 1;
            else if (res > 0)
                return -1;
            else
                return 0;
        }
    }

    public void setSemantic(boolean semantic) {
        this.semantic = semantic;
    }


}
