package sample;

import sun.awt.Mutex;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/** this class parse a text */
public class Parse {

    /** index the parsed terms */
    private Indexer idxr;

    /** the location of the stop words file */
    private String stopWordsPath;

    /** the terms of the text, and the frequency */
    private LinkedHashMap<String, Integer> docTerms;

    /** stemmer */
    private Stemmer stemmer;

    /**  empty constructor */
    public Parse(){
        idxr = new Indexer();
        this.stopWordsPath = SearchEngine.corpusPath+"\\stop_words.txt";
    }

    private LinkedHashMap<String, Integer> Parse(String text) {
        docTerms = new LinkedHashMap<>();
        ArrayList<String> list = tokenize(text);
        String currToken = "";
        String nextToken = "";
        String newToken = "";
        for(int i = 0 ; i < list.size() ; i++) {
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
                    newToken = parsePrice(currToken, "s");
                    i++;
                }
                else if (i < list.size()-2 && list.get(i+2).equalsIgnoreCase("dollars") &&
                    (newToken.equalsIgnoreCase("bn") || newToken.equalsIgnoreCase("m"))){
                    newToken = parsePrice(currToken, nextToken);
                    i = i+2;
                }
                else if (i < list.size()-3 && list.get(i+2).equalsIgnoreCase("U.S") && list.get(i+3).equalsIgnoreCase("dollars")) {
                    newToken = parsePrice(currToken, nextToken);
                    if (newToken.equalsIgnoreCase("millions") ||
                            newToken.equalsIgnoreCase("billions") ||
                            newToken.equalsIgnoreCase("trillions"))
                        i = i+3;
                    else
                        i = i+2;
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
                }else {
                    newToken = currToken;
                }
            }
            else if (currToken.length() > 1 && currToken.charAt(0) == '$' && isANumber(currToken.substring(1, currToken.length()))){
                newToken = parsePrice(currToken.substring(1, currToken.length()), nextToken);
                if (newToken.equalsIgnoreCase("millions") ||
                        newToken.equalsIgnoreCase("billions") ||
                        newToken.equalsIgnoreCase("trillions"))
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
                newToken = currToken;
                if (SearchEngine.stem){
                    stemmer = new Stemmer();
                    stemmer.add(currToken.toCharArray(), currToken.length());
                    stemmer.stem();
                    newToken = stemmer.toString();
                }
            }
            if (!docTerms.containsKey(newToken))
                docTerms.put(newToken, 1);
            else {
                int frequency = docTerms.get(newToken);
                docTerms.put(newToken, frequency + 1);
            }
        }
        HashSet<String> stopWords = getStopWords();
        Iterator it = docTerms.keySet().iterator();
        while(it.hasNext()) {
            String term = (String)it.next();
            if (stopWords.contains(term.toLowerCase())) {
                it.remove();
            }
        }
       return docTerms;
    }

    public void doneParsing(){
        idxr.Index();
    }

    public void parseDocument(Document doc){
        Parse(doc.getText());
        idxr.saveDetails(docTerms, doc);
    }

    public LinkedHashMap<String, Integer> parseQuery(String query){
        return Parse(query);
    }

    private String cleanTerm(String s) {
        int counter = 0;
        for(int i = 0; i < s.length(); i++) {
            if (!Character.isLetterOrDigit(s.charAt(i)) && s.charAt(i)!='$')
                counter++;
            else
                break;
        }
        s = s.substring(counter, s.length());
        counter = 0;
        for(int i = s.length()-1; i >= 0; i--) {
            if (!Character.isLetterOrDigit(s.charAt(i)) && s.charAt(i)!='$')
                counter++;
            else
                break;
        }
        s = s.substring(0, s.length()-counter);
        return s;
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

    private String parsePrice(String s, String next) {
        String numNoCommas = "";
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) != ',')
                numNoCommas = numNoCommas + s.charAt(i);
        }
        double num = Double.parseDouble(numNoCommas);
        String price = "";
        if (next.equalsIgnoreCase("billions") || next.equalsIgnoreCase("bn")) {
            num = num * 1000;
            if (num == (int) num)
                price = (int)num + " M Dollars";
            else
                price = num + " M Dollars";
        }else if (next.equalsIgnoreCase("trillions")) {
            num = num * 1000000;
            if (num == (int) num)
                price = (int)num + " M Dollars";
            else
                price = num+ " M Dollars";
        }else if (next.equalsIgnoreCase("millions") || next.equalsIgnoreCase("m")|| num >= 1000000) {
            num = num / 1000000;
            if (num == (int) num)
                price = (int)num + " M Dollars";
            else
                price = num+ " M Dollars";
        }else{
            price = s + " Dollars";
        }
        return price;
    }

    private boolean isANumber(String s) {
        boolean dotExist = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (i==0 && !Character.isDigit(c))
                return false;
            if(!Character.isDigit(c) && c != '.' && c != ',')
                return false;
            if(c == '.'){
                if (dotExist)
                    return false;
                dotExist = true;
            }
        }
        return true;
    }

    private boolean isFraction(String s) {
        for(char c : s.toCharArray())
            if(!Character.isDigit(c) && c != '/')
                return false;
        return true;
    }

    private String ParseNumWithCommas(String s){
        String numNoCommas = "";
        for (int i = 0; i < s.length(); i++){
            if (s.charAt(i) != ',')
                numNoCommas = numNoCommas + s.charAt(i);
        }
        double num = Double.parseDouble(numNoCommas);
        String Snum = "";
        if (num >= 1000000000){
            num = num/1000000000;
            if (num == (int)num)
                Snum = (int)num + "B";
            else
                Snum = num + "B";
        } else if(num >= 1000000){
            num = num/1000000;
            if (num == (int)num)
                Snum = (int)num + "M";
            else
                Snum = num + "M";
        } else if (num >= 1000){
            num = num/1000;
            if (num == (int)num)
                Snum = (int)num + "K";
            else
                Snum = num + "K";
        }
        return Snum;
    }

    private ArrayList<String> tokenize(String text) {
        String finalText = "";
        String[] splitByCommaSpace = text.split(", ");
        for(int i = 0; i < splitByCommaSpace.length; i++)
            finalText = finalText + splitByCommaSpace[i] + "~";
        String[] splitByDashSpace = finalText.split("- ");
        finalText = "";
        for(int i = 0; i < splitByDashSpace.length; i++)
            finalText = finalText + splitByDashSpace[i] + "~";
        String[] splitBySpaceDash = finalText.split(" -");
        finalText = "";
        for(int i = 0; i < splitBySpaceDash.length; i++)
            finalText = finalText + splitBySpaceDash[i] + "~";
        String[] splitByDotN = finalText.split("\\.\n");
        finalText = "";
        for(int i = 0; i < splitByDotN.length; i++)
            finalText = finalText + splitByDotN[i] + "~";
        String[] splitByDotSpace = finalText.split("\\. ");
        finalText = "";
        for(int i = 0; i < splitByDotSpace.length; i++)
            finalText = finalText + splitByDotSpace[i] + "~";
        String[] splitBySpace = finalText.split("\\s+");
        finalText = "";
        for(int i = 0; i < splitBySpace.length; i++)
            finalText = finalText + splitBySpace[i] + "~";
        String[] splitByDashDash = finalText.split("--");
        finalText = "";
        for(int i = 0; i < splitByDashDash.length; i++)
            finalText = finalText + splitByDashDash[i] + "~";
        StringTokenizer st = new StringTokenizer(finalText, "~:/`*\n!;+&|'\\()# []{}?\"");
        ArrayList<String> list = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String s = st.nextToken();
            if (!s.equals("")) {
                s = cleanTerm(s);
                if (!s.equals(""))
                    list.add(s);
            }
        }
        return list;
    }
}
