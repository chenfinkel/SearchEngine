package sample;

import java.util.HashMap;
import java.util.HashSet;

public class SearchEngine {

    private ReadFile readFile;
    private Parse parser;

    public SearchEngine(){
        readFile = new ReadFile("d:\\documents\\users\\chenfi\\Downloads\\corpus");
        parser = new Parse();
    }




}
