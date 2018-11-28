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
        int count = 0;
        for (File folder : folders) {
            try {
                File file = folder.listFiles()[0];
                Path path = file.toPath();
                String text = new String(Files.readAllBytes(path));
                String[] docs = text.split("</DOC>\n\n<DOC>\n");
                docs[0] = docs[0].split("<DOC>\n")[1];
                docs[docs.length-1] = docs[docs.length-1].split("</DOC>")[0];
                int docsParsed = 0;
                for (int i = 0; i < docs.length; i++) {
                    if (docsParsed == 10000){
                        parser.ParseDoc("index", "");
                        docsParsed = 0;
                        i--;
                    }
                    count ++ ;
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
                    String[] textsInDoc =  s.split("<TEXT>\n");
                    if (textsInDoc.length > 1) {
                        for (int j = 1; j < textsInDoc.length; j++) {
                            String textToParse = textsInDoc[j];
                            textToParse = textToParse.split("</TEXT>\n")[0];
                            parser.ParseDoc(textToParse, docNum);
                            docsParsed++;
                        }
                    }
                }
                parser.ParseDoc("index", "");

            }catch (IOException e) { e.printStackTrace(); }
        }
        System.out.println("Number of docs: " + count);
    }
}
