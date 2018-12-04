package sample;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

public class SearchEngine {

    private ReadFile readFile;

    public SearchEngine(){
        new File("posting").mkdirs();
        new File("docs").mkdirs();
        new File("cities").mkdirs();}

    public void setProps(String cp, String pp, boolean stem){
        readFile = new ReadFile(cp, pp, stem);
        readFile.read();

    }

    public void start() {
        readFile.read();
    }


}
