package sample;

import java.awt.event.ActionEvent;

public class Controller {
    private SearchEngine se;
    private View view;


    public Controller(){
        se = new SearchEngine();

    }

    public void startSE(String path, String postPath, boolean stem) {
        se.setProps(path,postPath,stem);
    }

    public void setView(View v) {
        view = v;
    }


}
