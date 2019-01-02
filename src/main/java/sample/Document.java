package sample;

import javafx.util.Pair;

import java.util.ArrayList;
import java.util.List;

/** this class represents a document in the corpus*/
public class Document {

    /**
     * the id of the document
     */
    private String DocID;
    /**
     * the text of the document
     */
    private String text;

    /**
     * the origin city of the document
     */
    private String city;

    /**
     * the language of the document
     */
    private String language;

    /**
     * the date of the document
     */
    private String date;

    /**
     * maximun term frequency in the document
     */
    private int maxTF;

    /**
     * size of the document
     */
    private int size;

    /**
     * number of unique terms in the documents
     */
    private int uniqueTerms;

    /**
     * sum of square TF*IDF of the terms of the document
     */
    private double sumOfSquareTFIDF;

    /**
     * the primary entities of the document
     */
    private List<Pair<String, Double>> primaryEntities;

    /**
     * constructor
     */
    public Document(String DocID, int maxTF, int uniqueTerms, String date, String city, String language, int size){
        this.DocID = DocID;
        this.maxTF = maxTF;
        this.uniqueTerms = uniqueTerms;
        this.date = date;
        this.city = city;
        this.language = language;
        this.size = size;
        this.primaryEntities = new ArrayList<>();
    }

    /**
     * getter
     */
    public List<Pair<String, Double>> getPrimaryEntities() {
        return primaryEntities;
    }

    /**
     * setter
     */
    public void setPrimaryEntities(List<Pair<String, Double>> primaryEntities) {
        this.primaryEntities.addAll(primaryEntities);
    }
    /**
     * constructor
     */
    public Document(String DocID){
        this.DocID = DocID;
    }

    /**
     * getter
     */
    public String getCity() {
        return city;
    }

    /**
     * getter
     */
    public int getMaxTF() { return maxTF; }

    /**
     * getter
     */
    public int getUniqueTerms() { return uniqueTerms; }

    /**
     * getter
     */
    public int getSize() { return size; }

    /**
     * getter
     */
    public String getDocID() {
        return DocID;
    }

    /**
     * getter
     */
    public String getLanguage() {
        return language;
    }

    /**
     * getter
     */
    public String getDate(){
        return date;
    }

    /**
     * getter
     */
    public String getText() {
        return text;
    }
    /**
     * setter
     */
    public void setDocID(String DocID){
        this.DocID = DocID;
    }

    /**
     * setter
     */
    public void setText(String text){
        this.text = text;
    }

    /**
     * setter
     */
    public void setCity(String city){
        this.city = city;
    }

    /**
     * setter
     */
    public void setLanguage(String language){
        this.language = language;
    }

    /**
     * setter
     */
    public void setMaxTF(int maxTF) { this.maxTF = maxTF; }

    /**
     * setter
     */
    public void setSize(int size) { this.size = size; }

    /**
     * setter
     */
    public void setUniqueTerms(int uniqueTerms) { this.uniqueTerms = uniqueTerms; }

    /**
     * setter
     */
    public void setDate(String date){
        this.date = date;
    }

    /** add a TF IDF grade of a term to the sum of square tf idf*/
    public void addTFIDF(double tfidf){
        sumOfSquareTFIDF = sumOfSquareTFIDF + tfidf*tfidf;
    }

    /**
     * setter
     */
    public void setSumOfSquareTFIDF(double sum){
        sumOfSquareTFIDF = sum;
    }
    /**
     * getter
     */
    public double getSumOfSquareTFIDF() {
        return sumOfSquareTFIDF;
    }
}
