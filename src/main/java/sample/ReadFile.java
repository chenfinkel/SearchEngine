package sample;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

public class ReadFile {

    private Parse parser;
    private String path;


    public ReadFile(){
        path = "";
    }

    public ReadFile(String path) {
        this.path = path;
        parser = new Parse("C:\\Users\\User\\Documents\\שנה ג\\אחזור\\מנוע\\stop_words.txt");
    }

    /**
     * Read all file and separate them into documents
     */
    public void read(){
        String newPath = "", data = "";
        File mainFolder = new File(path);
        File[] folders = mainFolder.listFiles();
        FileReader fr;
        int docsParsed = 0;
        for (File folder : folders) {
            try {
                File file = folder.listFiles()[0];
                String fileBody = FileUtils.readFileToString(file);
                String[] docs = StringUtils.substringsBetween(fileBody, "<DOC>", "</DOC>");
                for (int i = 0; i < docs.length; i++) {
                    if (docsParsed == 10000){
                        parser.ParseDoc("index", "");
                        docsParsed = 0;
                        i--;
                    }
                    String docID = StringUtils.substringBetween(docs[i], "<DOCNO>", "</DOCNO>");
                    String[] textsInDoc = StringUtils.substringsBetween(docs[i], "<TEXT>", "</TEXT>");
                    if (textsInDoc != null) {
                        for (int j = 0; j < textsInDoc.length; j++) {
                            String textToParse = textsInDoc[j];
                            parser.ParseDoc(textToParse, docID);
                            docsParsed++;
                        }
                    }
                }
            }catch (IOException e) { e.printStackTrace(); }
        }
    }
}
