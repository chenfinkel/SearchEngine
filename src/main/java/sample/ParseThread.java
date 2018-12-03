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
                String docID2 = StringUtils.substringBetween(docID, " ", " ");
                if (!docID2.equals(""))
                    docID = docID2;
                String[] textsInDoc = StringUtils.substringsBetween(docs[i], "<TEXT>", "</TEXT>");
                if (textsInDoc != null) {
                    for (int j = 0; j < textsInDoc.length; j++) {
                        String textToParse = textsInDoc[j];
                        parser.ParseDoc(textToParse, docID, false);
                    }
                }
            }
            parser.ParseDoc("index", "index", false);
        }catch (IOException e) { e.printStackTrace(); }
    }
}
