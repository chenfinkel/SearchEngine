package sample;


import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    }

    public void read(){
        String newPath = "", data = "";
        File mainFolder = new File(path);
        File[] folders = mainFolder.listFiles();
        FileReader fr;
        for (File folder : folders) {
            try
            {
                File file = folder.listFiles()[0];
                data = new String(Files.readAllBytes(Paths.get(file.getPath())));
                Document document = Jsoup.parse(data);
                Elements elements = document.getElementsByTag("DOC");
                for (Element element : elements) {
                    String id = element.getElementsByTag("DOCNO").text();
                    String text = element.getElementsByTag("TEXT").text();
                    parser.ParseDoc(text, id);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
