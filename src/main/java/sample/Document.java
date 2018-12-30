package sample;

public class Document {

    private String DocID;

    private String text;

    private String city;

    private String language;

    private String date;

    private int maxTF;

    private int size;

    private int uniqueTerms;

    private double sumOfSquareTFIDF;

    public Document(String DocID, int maxTF, int uniqueTerms, String date, String city, String language, int size){
        this.DocID = DocID;
        this.maxTF = maxTF;
        this.uniqueTerms = uniqueTerms;
        this.date = date;
        this.city = city;
        this.language = language;
        this.size = size;
    }
    public Document(String DocID){
        this.DocID = DocID;
    }

    public String getCity() {
        return city;
    }

    public int getMaxTF() { return maxTF; }

    public int getUniqueTerms() { return uniqueTerms; }

    public int getSize() { return size; }

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

    public void setMaxTF(int maxTF) { this.maxTF = maxTF; }

    public void setSize(int size) { this.size = size; }

    public void setUniqueTerms(int uniqueTerms) { this.uniqueTerms = uniqueTerms; }

    public void setDate(String date){
        this.date = date;
    }

    public void addTFIDF(double tfidf){
        sumOfSquareTFIDF = sumOfSquareTFIDF + tfidf*tfidf;
    }

    public void setSumOfSquareTFIDF(double sum){
        sumOfSquareTFIDF = sum;
    }

    public double getSumOfSquareTFIDF() {
        return sumOfSquareTFIDF;
    }
}
