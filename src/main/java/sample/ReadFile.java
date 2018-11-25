package sample;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.sun.deploy.util.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.*;
import org.jsoup.select.Elements;

public class ReadFile {

    private Parse parser;
    private String path;


    public ReadFile(){
        path = "";
    }

    public ReadFile(String path) {
        this.path = path;
        parser = new Parse("C:\\Users\\chenfi\\IdeaProjects\\stop_words.txt");
    }

    /**
     * Read all file and separate them into documents
     */
    public void read(){
        String newPath = "", data = "";
        File mainFolder = new File(path);
        File[] folders = mainFolder.listFiles();
        FileReader fr;
        int count = 0;
        for (File folder : folders) {
            try {
                File file = folder.listFiles()[0];
                Path path = file.toPath();
                String text = new String(Files.readAllBytes(path));
                String[] docs = text.split("</DOC>\n\n<DOC>\n");
                docs[0] = docs[0].split("<DOC>\n")[1];
                docs[docs.length-1] = docs[docs.length-1].split("</DOC>")[0];
                for (int i = 0; i < docs.length; i++) { count ++ ;
                    String[] splitToDocNum = docs[i].split("<DOCNO>");
                    String docNum = "";
                    try {
                        docNum = splitToDocNum[1];
                    }catch(Exception e) {
                        e.printStackTrace();
                        System.out.println(docs[i]);
                    }
                    String[] subText = docNum.split("</DOCNO>\n");
                    docNum = subText[0];
                    String s = subText[1];
                    String[] textInDoc =  s.split("<TEXT>\n");
                    if (textInDoc.length > 1) {
                        String textToParse = textInDoc[1];
                        textToParse = textToParse.split("</TEXT>\n")[0];
                        parser.ParseDoc(textToParse, docNum);
                    }
                }

            }catch (IOException e) { e.printStackTrace(); }
        }
        System.out.println(count);
    }
}
