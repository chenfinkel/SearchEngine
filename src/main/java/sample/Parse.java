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
            price = s + "Dollars";
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
            if (Character.isDigit(s.charAt(i)))
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
        String ans = "";
        try {
            num = num.substring(0, num.length() - counter);
            ans = num.substring(0, digitsBeforeDot) + "." + num.substring(digitsBeforeDot, num.length()) + suffix;
        }catch (Exception e){
            System.out.println("s: " + s);
            System.out.println("Base: " + base);
            System.out.println("Suffix: " + suffix);
            System.out.println("Digits: "+ digitsBeforeDot);
            System.out.println("Counter: " + counter);
            e.printStackTrace();
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
        StringTokenizer st = new StringTokenizer(finalText, "~\n!; ()[]{}?\"");
        ArrayList<String> list = new ArrayList<>();
        while (st.hasMoreTokens())
            list.add(st.nextToken());
        return list;
    }
}
