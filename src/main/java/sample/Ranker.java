package sample;

import org.apache.commons.lang3.StringUtils;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class Ranker {

    private double avdl;

    public Ranker(double avdl) {
        this.avdl = avdl;
    }

    public List<Document> Rank(String title) {
        try {
            Parse p = new Parse();
            LinkedHashMap<String, Integer> queryTerms = p.parseQuery(title);
            LinkedHashMap<Term, String> postingLines = getPostingLines(queryTerms);
            HashMap<Document, Double> ranks = new HashMap<>();
            Iterator<Document> it = SearchEngine.documents.values().iterator();
            while (it.hasNext()) {
                Document document = it.next();
                double rank = CalcDocRank(queryTerms, postingLines, document);
                if (rank > 0)
                    ranks.put(document, rank);
            }
            List<Map.Entry<Document, Double>> sorted = new LinkedList<>(ranks.entrySet());
            Collections.sort(sorted, new Ranker.sort());
            List<Document> result = new ArrayList<>();
            for (int i = 0; i < 50 && i < sorted.size(); i++) {
                Map.Entry<Document, Double> entry = sorted.get(i);
                result.add(entry.getKey());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private LinkedHashMap<Term, String> getPostingLines(LinkedHashMap<String, Integer> queryTerms) {
        LinkedHashMap<Term, String> postingLines = new LinkedHashMap<>();
        Iterator<Map.Entry<String, Integer>> it = queryTerms.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Integer> entry = it.next();
            String term = entry.getKey();
            Term t = SearchEngine.dictionary.get(term.toLowerCase());
            if (t == null){
                t = SearchEngine.dictionary.get(term.toUpperCase());
            }
            if (t != null) {
                try {
                    Stream<String> lines = Files.lines(Paths.get(SearchEngine.postingPath + "\\" + Character.toLowerCase(term.charAt(0)) + ".txt"));
                    String line = lines.skip(t.postingLine - 1).findFirst().get();
                    postingLines.put(t, line);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return postingLines;
    }

    private double CalcDocRank(LinkedHashMap<String, Integer> queryTerms, LinkedHashMap<Term, String> postingLines, Document doc) {
        double ans = 0;
        Iterator<Map.Entry<Term, String>> it = postingLines.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Term, String> entry = it.next();
            Term t = entry.getKey();
            try {
                String tmp = "";
                String tfDoc = StringUtils.substringBetween(entry.getValue(), doc.getDocID() + "*", "!");
                if (tfDoc != null) {
                    int tfDocNum = Integer.parseInt(tfDoc);
                    ans = ans + BM25(t, doc, tfDocNum, queryTerms.get(t.getId()));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return ans;
    }

    private double BM25(Term t, Document doc, int tfDoc, int tfQuery) {
        double B = 0.75;
        int K = 2;
        int length = doc.getSize();
        int df = t.docFreq;
        int numOfDocs = SearchEngine.documents.size();
        double firstPart = (((K + 1) * tfDoc) / (tfDoc + K * (1 - B + (B * (length / avdl)))));
        double secondPart = Math.log(numOfDocs + 1 / df);
        return (tfQuery * firstPart * secondPart);
    }

    public class sort implements Comparator<Object> {
        public int compare(Object o1, Object o2) {
            Map.Entry<Document, Double> s1 = (Map.Entry<Document, Double>) o1;
            Map.Entry<Document, Double> s2 = (Map.Entry<Document, Double>) o2;
            double res = s1.getValue() - s2.getValue();
            if (res > 0)
                return 1;
            else if (res < 0)
                return -1;
            else
                return 0;
        }
    }


}
