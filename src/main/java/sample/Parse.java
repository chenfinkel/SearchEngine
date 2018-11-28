package sample;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Parse {


    private Indexer idxr;
    private String stopWordsPath;
    //HashMap contains the terms of the document, and the location
    private HashMap<String, ArrayList<Integer>> docTerms;


    public Parse() {
        idxr = new Indexer();
    }

    public Parse(String stopWordsPath){
        idxr = new Indexer();
        this.stopWordsPath = stopWordsPath;
    }

    /**
     *
     * @param text is the document text
     * @param docID is the id of the document
     */

    public void ParseDoc(String text, String docID) {
        if(text.equals("index")) {
            idxr.Index(null, docID);
        }
        docTerms = new HashMap<>();
        ArrayList<String> list = tokenize(text);
        String currToken = "";
        String nextToken = "";
        String newToken = "";
        Term t = null;
        for(int i = 0 ; i < list.size() ; i++) {
            int j = i;
            currToken = list.get(i);
            if (i != list.size() -1)
                nextToken = list.get(i+1);
            if(isANumber(currToken)) {
                if (nextToken.equalsIgnoreCase("percent") || nextToken.equalsIgnoreCase("percentage")) {
                    newToken = currToken + "%";
                    i++;
                }
                else if (nextToken.equalsIgnoreCase("kilometers") || nextToken.equals("km")){
                    newToken = currToken + " km";
                    i++;
                }
                else if (nextToken.equalsIgnoreCase("Dollars")) {
                    newToken = parsePrice(currToken);
                    i++;
                }
                else if (i < list.size()-3 && list.get(i+2).equals("U.S") && list.get(i+3).equalsIgnoreCase("dollars")) {
                    boolean isPrice = true;
                    String term = "";
                    if (nextToken.equalsIgnoreCase("billion"))
                        term = currToken + "000";
                    else if (nextToken.equalsIgnoreCase("trillion"))
                        term = currToken + "000000";
                    else if (nextToken.equalsIgnoreCase("million"))
                        term = currToken;
                    else
                        isPrice = false;
                    if (isPrice) {
                        newToken = term + " M Dollars";
                        i = i+3;
                    }
                }
                else if (currToken.indexOf(',') >= 0) {
                    newToken = ParseNumWithCommas(currToken);
                }
                else if (isFraction(nextToken)) {
                    String num = currToken + " " + nextToken;
                    if (i < list.size()-2 && list.get(i+2).equalsIgnoreCase("Dollars")) {
                        num = num + " Dollars";
                        i++;
                    }
                    newToken = num;
                    i++;
                }
                else if (nextToken.equalsIgnoreCase("Trillion")) {
                    newToken = currToken + "000B";
                    i++;
                }
                else if (nextToken.equalsIgnoreCase("Billion")) {
                    newToken = currToken + "B";
                    i++;
                }
                else if (nextToken.equalsIgnoreCase("Million")) {
                    newToken = currToken + "M";
                    i++;
                }
                else if (nextToken.equalsIgnoreCase("Thousand")) {
                    newToken = currToken + "K";
                    i++;
                }
                else if(currToken.length() == 2 && isDay(currToken) && !isMonth(nextToken).equals("false")){
                    String month = isMonth(nextToken);
                    newToken = month + "-" + currToken;
                    i++;
                }
            }
            else if (currToken.charAt(0) == '$' && isANumber(currToken.substring(1, currToken.length()))){
                i++;
                newToken = currToken.substring(1, currToken.length());
                if (nextToken.equalsIgnoreCase("million"))
                    newToken = currToken + " M Dollars";
                else if (nextToken.equalsIgnoreCase("billion"))
                    newToken = currToken+"000 M Dollars";
                else if (nextToken.equalsIgnoreCase("trillion"))
                    newToken = currToken+"000000 M Dollars";
                else {
                    newToken = parsePrice(currToken);
                    i--;
                }
            }
            else if (isMillions(currToken) && nextToken.equalsIgnoreCase("Dollars")){
                newToken = currToken.substring(0, currToken.length()-1) + " M Dollars";
                i++;
            }
            else if (isBillions(currToken, nextToken) && i+2 < list.size() && list.get(1+2).equalsIgnoreCase("Dollars")){
                newToken = currToken.substring(0, currToken.length()-2) + "000 M Dollars";
                i++;
            }
            else if (!isMonth(currToken).equals("false")){
                boolean isDate = true;
                String month = isMonth(currToken);
                String term = "";
                if (isYear(nextToken))
                    term = nextToken + "-" + month;
                else if (nextToken.length()==2 && isDay(nextToken))
                    term = currToken+ "-" + nextToken;
                else
                    isDate = false;
                if (isDate) {
                    i++;
                    newToken = term;
                }
            } else {
                if (Character.isUpperCase(currToken.charAt(0))) {
                    String lowerCase = currToken.toLowerCase();
                    if (docTerms.containsKey(lowerCase))
                        newToken = lowerCase;
                    else
                        newToken = currToken.toUpperCase();
                } else {
                    String upperCase = currToken.toUpperCase();
                    if (docTerms.containsKey(upperCase))
                        docTerms.remove(upperCase);
                }
            }
            if (!docTerms.containsKey(newToken))
                docTerms.put(newToken, new ArrayList<Integer>());
            docTerms.get(newToken).add(j);
        }
        HashSet<String> stopWords = getStopWords();
        Iterator it = docTerms.keySet().iterator();
        while(it.hasNext()) {
            String term = (String)it.next();
            if (stopWords.contains(term)) {
                it.remove();
            }
        }
       idxr.Index(docTerms, docID);
    }

    private boolean isYear(String s) {
        if (s.length() == 4) {
            for (int i = 0; i < s.length(); i++) {
                if (!Character.isDigit(s.charAt(i)))
                    return false;
            }
            return true;
        }
        return false;
    }

    private boolean isDay(String s) {
        int day = -1;
        if (Character.isDigit(s.charAt(0)) && Character.isDigit(s.charAt(1)))
            day = Integer.parseInt(s);
        if (day >= 0 && day <= 31)
            return true;
        return false;
    }

    private HashSet<String> getStopWords(){
        HashSet<String> sw = new HashSet<>();
        Path path = Paths.get(stopWordsPath);
        try {
            String text = new String(Files.readAllBytes(path));
            String[] stopWordsArray = text.split(System.lineSeparator());
            for (int i = 0; i < stopWordsArray.length; i++)
                sw.add(stopWordsArray[i]);
        }catch(Exception e){ e.printStackTrace(); }
        return sw;
    }

    private String isMonth(String s){
        if (s.length() == 3) {
            if (s.equalsIgnoreCase("Jen"))
                return "01";
            if (s.equalsIgnoreCase("Feb"))
                return "02";
            if (s.equalsIgnoreCase("Mar"))
                return "03";
            if (s.equalsIgnoreCase("Apr"))
                return "04";
            if (s.equalsIgnoreCase("May"))
                return "05";
            if (s.equalsIgnoreCase("Jun"))
                return "06";
            if (s.equalsIgnoreCase("Jul"))
                return "07";
            if (s.equalsIgnoreCase("Aug"))
                return "08";
            if (s.equalsIgnoreCase("Sep"))
                return "09";
            if (s.equalsIgnoreCase("Oct"))
                return "10";
            if (s.equalsIgnoreCase("Nov"))
                return "11";
            if (s.equalsIgnoreCase("Dec"))
                return "12";
        }
        if (s.equalsIgnoreCase("January"))
            return "01";
        if (s.equalsIgnoreCase("February"))
            return "02";
        if (s.equalsIgnoreCase("March"))
              return "03";
        if (s.equalsIgnoreCase("April"))
            return "04";
        if (s.equalsIgnoreCase("June"))
            return "06";
        if (s.equalsIgnoreCase("July"))
            return "07";
        if (s.equalsIgnoreCase("August"))
            return "08";
        if (s.equalsIgnoreCase("September"))
            return "09";
        if (s.equalsIgnoreCase("October"))
            return "10";
        if (s.equalsIgnoreCase("November"))
            return "11";
        if (s.equalsIgnoreCase("December"))
            return "12";
        return "false";
    }

    private String parsePrice(String s) {
        int counter = 0;
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i)))
                counter++;
        }
        if (counter < 7 || s.indexOf('.') >= 0) {
            return s+" Dollars";
        }
        String value = parseNumberByBase(s, 6, " M");
        return value + " Dollars";
    }

    private boolean isANumber(String s) {
        for(char c : s.toCharArray())
            if(!Character.isDigit(c) && c != '.' && c != ',')
                return false;
        return true;
    }

    private boolean isFraction(String s) {
        for(char c : s.toCharArray())
            if(!Character.isDigit(c) && c != '/')
                return false;
        return true;
    }

    private String ParseNumWithCommas(String s) {
        String ans = "";
        s = s.replace(',', '.');
        if (s.length() > 4 && s.length() < 8)
            ans = parseThousandsWithCommas(s);
        else if (s.length() > 8 && s.length() < 12)
            ans = parseMillionsWithCommas(s);
        else if (s.length() > 12)
            ans = parseNumberByBase(s, 9, "B");
        return ans;
    }

    private String parseThousandsWithCommas(String s) {
        String ans = "";
        if ((s.substring(s.length()-3,s.length())).equals("000"))
            ans = s.substring(0,s.length()-4) + "K";
        else if ((s.substring(s.length()-2,s.length())).equals("00"))
            ans = s.substring(0,s.length()-2) + "K";
        else if (s.charAt(s.length()-1) == '0')
            ans = s.substring(0,s.length()-1) + "K";
        else
            ans = s + "K";
        return ans;
    }

    private String parseMillionsWithCommas(String s) {
        String ans = "";
        if ((s.substring(s.length()-7,s.length())).equals("000.000"))
            ans = s.substring(0,s.length()-8) + "M";
        else if ((s.substring(s.length()-6,s.length())).equals("00.000"))
            ans = s.substring(0,s.length()-7) + "M";
        else if ((s.substring(s.length()-5,s.length())).equals("0.000"))
            ans = s.substring(0,s.length()-6) + "M";
        else if ((s.substring(s.length()-3,s.length())).equals("000"))
            ans = s.substring(0,s.length()-4) + "M";
        else if ((s.substring(s.length()-2,s.length())).equals("00"))
            ans = s.substring(0,s.length()-4) + s.substring(s.length()-3,s.length()-2) + "M";
        else if (s.charAt(s.length()-1) == '0')
            ans = s.substring(0,s.length()-4) + s.substring(s.length()-3,s.length()-1) + "M";
        else
            ans = s.substring(0,s.length()-4) + s.substring(s.length()-3,s.length()) + "M";
        return ans;
    }

    private String parseNumberByBase(String s, int base, String suffix) {
        String num = "";
        for (int i = 0; i < s.length(); i++)
            if (s.charAt(i) != '.' && s.charAt(i) != ',')
                num = num + s.charAt(i);
        int counter = 0;
        int digitsBeforeDot = num.length() - base;
        for (int i = num.length()-1; i >= base; i--) {
            if (num.charAt(i) == '0')
                counter++;
            else
                break;
        }
        if (counter == base)
            return num.substring(0, digitsBeforeDot) + suffix;
        num = num.substring(0, num.length()-counter);
        String ans = num.substring(0, digitsBeforeDot) + "." + num.substring(digitsBeforeDot, num.length()) + suffix;
        return ans;
    }

    private boolean isMillions(String s){
        if (s.charAt(s.length()-1) == 'm' && isANumber(s.substring(0, s.length()-1)))
            return true;
        return false;
    }

    private boolean isBillions(String s, String next) {
        if (isANumber(s) && next.equalsIgnoreCase("bn"))
            return true;
        return false;
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
        String[] splitByDotN = finalText.split("\\.\n");
        finalText = "";
        for(int i = 0; i < splitByDotN.length; i++)
            finalText = finalText + splitByDotN[i] + " ";
        String[] splitByDotSpace = finalText.split("\\. ");
        finalText = "";
        for(int i = 0; i < splitByDotSpace.length; i++)
            finalText = finalText + splitByDotSpace[i] + " ";
        StringTokenizer st = new StringTokenizer(finalText, "\n!; ()[]{}?\"");
        ArrayList<String> list = new ArrayList<>();
        while (st.hasMoreTokens())
            list.add(st.nextToken());
        return list;
    }
}
