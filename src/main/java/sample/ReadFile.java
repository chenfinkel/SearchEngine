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
        parser = new Parse();
    }

    /**
     * Read all file and separate them into documents
     */
    public void read(){
        String newPath = "", data = "";
        File mainFolder = new File(path);
        File[] folders = mainFolder.listFiles();
        FileReader fr;
        for (File folder : folders) {
            try {
                File file = folder.listFiles()[0];
                Path path = file.toPath();
                String text = new String(Files.readAllBytes(path));
                String[] docs = text.split("</DOC>\n\n<DOC>\n");
                docs[0] = docs[0].split("<DOC>\n")[1];
                docs[docs.length-1] = docs[docs.length-1].split("</DOC>")[0];
                for (int i = 0; i < docs.length; i++) {
                    String[] splitToDocNum = docs[i].split("<DOCNO> ");
                    String docNum = splitToDocNum[1];
                    String[] subText = docNum.split(" </DOCNO>\n");
                    docNum = subText[0];
                    String textToParse = subText[1].split("<TEXT>\n")[1];
                    textToParse = textToParse.split("</TEXT>\n")[0];
                    parser.ParseDoc(textToParse, docNum);
                }
            }catch (IOException e) {
                e.printStackTrace();
            }






                /**
                String[] docs = text.split("</DOC>\n");
                for (int i = 0; i < docs.length; i++) {
                    docs[i] = docs[i].split("\n<DOC>")[0];
                }
                for (int i = 0; i < docs.length; i++) {

                }
            }catch (Exception e) { e.printStackTrace(); }
            /*try
            {
                File file = folder.listFiles()[0];
                data = new String(Files.readAllBytes(Paths.get(file.getPath())));
                Document document = Jsoup.parse(data);
                Elements elements = document.getElementsByTag("DOC");
                for (Element doc : elements) {
                    String id = elements.first().getElementsByTag("DOCNO").text();
                    String text = elements.first().getElementsByTag("TEXT").text();
                    System.out.println(text);
                    parser.ParseDoc(text, id);

                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }*/

        }
    }
}
