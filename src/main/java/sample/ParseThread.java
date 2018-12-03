package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class ParseThread extends Thread {

    private Parse parser;

    private File folder;

    public ParseThread(){
        parser = new Parse();
    }

    public ParseThread(File folder){
        parser = new Parse("D:\\searchEngine\\stop_words.txt");
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
                String[] cityWords = city.split(" ");
                if (cityWords.length > 1)
                    city = cityWords[0].toUpperCase();
                String date = StringUtils.substringBetween(docs[i],"<DATE1>", " </DATE1>");
                date = removeSpaces(date);
                String language = StringUtils.substringBetween(docs[i],"<F P=105>", " </F>");
                language = removeSpaces(language);
                String[] textsInDoc = StringUtils.substringsBetween(docs[i], "<TEXT>", "</TEXT>");
                if (textsInDoc != null) {
                    for (int j = 0; j < textsInDoc.length; j++) {
                        String textToParse = textsInDoc[j];
                        parser.ParseDoc(textToParse, docID, city, date, language, false);
                    }
                }
            }
            parser.ParseDoc("index", "index", "", "", "", false);
        }catch (IOException e) { e.printStackTrace(); }
    }

    private String removeSpaces(String s){
        if (s == null)
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
