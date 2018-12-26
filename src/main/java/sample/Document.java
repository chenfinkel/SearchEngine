package sample;

public class Document {

    private String DocID;

    private String text;

    private String city;

    private String language;

    private String date;


    public Document(String DocID){
        this.DocID = DocID;
    }

    public String getCity() {
        return city;
    }

    public String getDocID() {
        return DocID;
    }

    public String getLanguage() {
        return language;
    }

    public String getDate(){
        return date;
    }

    public String getText() {
        return text;
    }

    public void setDocID(String DocID){
        this.DocID = DocID;
    }

    public void setText(String text){
        this.text = text;
    }
    public void setCity(String city){
        this.city = city;
    }
    public void setLanguage(String language){
        this.language = language;
    }

    public void setDate(String date){
        this.date = date;
    }
}
