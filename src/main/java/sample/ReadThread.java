package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.IOException;

/** a thread that parses a single folder */
public class ReadThread extends Thread {

    /** parser */
    private Parse parser;

    /** the folder to parse */
    private File folder;


    /** empty constructor */
    public ReadThread(){

    }

    /** constructor */
    public ReadThread(File folder){
        parser = new Parse(SearchEngine.corpusPath + "\\stop_words.txt");
        this.folder = folder;
    }

    /**
     * override
     * reads all folder's files, seperates the main tags for each document and send if to indexing
     */
    public void run(){
        try {
            File file = folder.listFiles()[0];
            String fileBody = FileUtils.readFileToString(file);
            String[] docs = StringUtils.substringsBetween(fileBody, "<DOC>", "</DOC>");
            for (int i = 0; i < docs.length; i++) {
                String docID = StringUtils.substringBetween(docs[i], "<DOCNO>", "</DOCNO>");
                docID = removeSpaces(docID);
                Document doc = new Document(docID);
                String city = StringUtils.substringBetween(docs[i],"<F P=104>", "</F>");
                city = removeSpaces(city);
                if(Character.isLetter(city.charAt(0))) {
                    city = city.toUpperCase();
                    String[] cityWords = city.split("\\s+");
                    if (cityWords.length > 1)
                        city = cityWords[0];
                }
                else
                    city = "X";
                doc.setCity(city);
                String date = StringUtils.substringBetween(docs[i],"<DATE1>", " </DATE1>");
                date = removeSpaces(date);
                doc.setDate(date);
                String language = StringUtils.substringBetween(docs[i],"<F P=105>", " </F>");
                language = removeSpaces(language);
                char langfirst = language.charAt(0);
                if (langfirst != 'X' && Character.isLetter(langfirst) && Character.isUpperCase(langfirst)) {
                    String[] langWords = language.split("\\s+");
                    if (langWords.length > 1)
                        language = langWords[0];
                } else
                    language = "X";
                doc.setLanguage(language);
                String[] textsInDoc = StringUtils.substringsBetween(docs[i], "<TEXT>", "</TEXT>");
                if (textsInDoc != null) {
                    for (int j = 0; j < textsInDoc.length; j++) {
                        String textToParse = textsInDoc[j];
                        doc.setText(textToParse);
                        parser.parseDocument(doc);
                    }
                }
            }
            parser.doneParsing();
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
        while (s.charAt(i) == ' ' || s.charAt(i) == ',') {
            end++;
            i--;
        }
        return s.substring(start, s.length()-end);
    }
}
