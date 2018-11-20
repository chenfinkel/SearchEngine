package sample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

public class Parse {

    private Indexer idxr;

    public Parse() {
        idxr = new Indexer();
    }

    /**
     *
     * @param text is the document text
     * @param id is the id of the document
     */

    public void ParseDoc(String text, String id) {
        //HashMap contains the terms of the document, and the location
        HashMap<Term, Integer> docTerms = new HashMap<>();
        ArrayList<String> list = tokenize(text);
        String currToken = "";
        String nextToken = "";
        ////////////////stop words!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! stop word and space for not deleting terms with it.
        for(int i = 0 ; i < list.size() ; i++) {
            currToken = list.get(i);
            if(isANumber(currToken)) {
                nextToken = list.get(i+1);
                if (currToken.indexOf(',') >= 0) {
                    String num = ParseNumWithCommas(currToken);
                    Term t = new Term(num);
                    docTerms.put(t, i);
                    continue;
                }
                if (nextToken == "Trillion") {
                    currToken = currToken + "00B";
                    Term t = new Term(currToken);
                    docTerms.put(t, i);
                    continue;
                }
                if (nextToken == "Billion") {
                    currToken = currToken + "B";
                    Term t = new Term(currToken);
                    docTerms.put(t, i);
                    continue;
                }
                if (nextToken == "Million") {
                    currToken = currToken + "M";
                    Term t = new Term(currToken);
                    docTerms.put(t, i);
                    continue;
                }
                if (nextToken == "Thousand") {
                    currToken = currToken + "K";
                    Term t = new Term(currToken);
                    docTerms.put(t, i);
                    continue;
                }
            }
        }
        System.out.println("hi");
    }

    private boolean isANumber(String s) {
        for(char c : s.toCharArray())
            if(!Character.isDigit(c) && c != '.' && c != ',')
                return false;
        return true;
    }

    private String ParseNumWithCommas(String s) {
        String ans = "";
        s = s.replace(',', '.');
        if (s.length() > 3 && s.length() < 7) {
            if ((s.substring(s.length()-3,s.length())).equals("000"))
                ans = s.substring(0,s.length()-4) + "K";
            else if ((s.substring(s.length()-2,s.length())).equals("00"))
                ans = s.substring(0,s.length()-2) + "K";
            else if (s.charAt(s.length()-1) == '0')
                ans = s.substring(0,s.length()-1) + "K";
            else
                ans = s + "K";
        }
        return ans;
    }

    /**
     * The method tokenizes the text and returns a list of the tokens.
     * @param text is the original text we get from the readFile
     * @returns a string tokenizer thats holding the tokens after tokenizing them
     */
    private ArrayList<String> tokenize(String text) {
        String finalText = "";
        String[] splitByCommaSpace = text.split(", ");
        for(int i = 0; i < splitByCommaSpace.length; i++)
            finalText = finalText + splitByCommaSpace[i] + " ";
        String[] splitByDashSpace = finalText.split("- ");
        finalText = "";
        for(int i = 0; i < splitByDashSpace.length; i++)
            finalText = finalText + splitByDashSpace[i] + " ";
        String[] splitBySpaceDash = finalText.split(" -");
        finalText = "";
        for(int i = 0; i < splitBySpaceDash.length; i++)
            finalText = finalText + splitBySpaceDash[i] + " ";
        String[] splitByDotSpace = finalText.split("/. ");
        finalText = "";
        for(int i = 0; i < splitByDotSpace.length; i++)
            finalText = finalText + splitByDotSpace[i] + " ";
        StringTokenizer st = new StringTokenizer(finalText, "\n!; ()[]{}?/\"");
        ArrayList<String> list = new ArrayList<>();
        while (st.hasMoreTokens())
            list.add(st.nextToken());
        return list;
    }
}
