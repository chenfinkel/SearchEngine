package sample;

import java.util.LinkedHashSet;

public class City {

    private String city;

    private String state;

    private String population;

    private String currency;

    private LinkedHashSet<Document> documents;

    public City(String city){
        this.city = city;
        this.documents = new LinkedHashSet<>();
    }

    public City(String city, String state, String population, String currency){
        this.city = city;
        this.state = state;
        this.population = population;
        this.currency = currency;
        this.documents = new LinkedHashSet<>();
    }

    public String getCity() {
        return city;
    }

    public String getCurrency() {
        return currency;
    }

    public String getPopulation() {
        return population;
    }

    public String getState() {
        return state;
    }

    public LinkedHashSet<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(LinkedHashSet<Document> documents) {
        this.documents.addAll(documents);
    }
}
