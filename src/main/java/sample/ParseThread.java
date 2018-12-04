package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class ParseThread extends Thread {

    private Parse parser;

    private File folder;

    private boolean stem;

    public ParseThread(){
        parser = new Parse();
    }

    public ParseThread(File folder, String corpusPath, String postPath, boolean stem){
        parser = new Parse(corpusPath + "\\stop_words.txt");
        this.stem = stem;
        this.folder = folder;
    }

    public void run(){
        try {
            File file = folder.listFiles()[0];
            String fileBody = FileUtils.readFileToString(file);
            String[] docs = StringUtils.substringsBetween(fileBody, "<DOC>", "</DOC>");
            for (int i = 0; i < docs.length; i++) {
                String docID = StringUtils.substringBetween(docs[i], "<DOCNO>", "</DOCNO>");
                docID = removeSpaces(docID);
                String city = StringUtils.substringBetween(docs[i],"<F P=104>", "</F>");
                city = removeSpaces(city);
                city = city.toUpperCase();
                String[] cityWords = city.split("\\s+");
                if (cityWords.length > 1)
                    city = cityWords[0];
                String date = StringUtils.substringBetween(docs[i],"<DATE1>", " </DATE1>");
                date = removeSpaces(date);
                String language = StringUtils.substringBetween(docs[i],"<F P=105>", " </F>");
                language = removeSpaces(language);
                String[] textsInDoc = StringUtils.substringsBetween(docs[i], "<TEXT>", "</TEXT>");
                if (textsInDoc != null) {
                    for (int j = 0; j < textsInDoc.length; j++) {
                        String textToParse = textsInDoc[j];
                        parser.ParseDoc(textToParse, docID, city, date, language, stem);
                    }
                }
            }
            parser.ParseDoc("index", "index", "", "", "", stem);
        }catch (IOException e) { e.printStackTrace(); }
    }

    private String removeSpaces(String s){
        if (s == null || s.equals(""))
            return "X";
        String ans = "";
        int start = 0;
        int end = 0;
        int i = 0;
        while (s.charAt(i) == ' ') {
            start++;
            i++;
        }
        i = s.length()-1;
        while (s.charAt(i) == ' ') {
            end++;
            i--;
        }
        return s.substring(start, s.length()-end);
    }
}
