package sample;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.decimal4j.util.DoubleRounder;

import java.net.URL;
import java.util.LinkedHashSet;

/** this class represents a city from the corpus*/
public class City {

    /** the name of the city*/
    private String city;

    /** the state of the city */
    private String state;

    /** size of population in the city*/
    private String population;

    /** the currency used in the city*/
    private String currency;

    /** the documents that are from the city or contains the city in text*/
    private LinkedHashSet<Document> documents;

    /** constructor*/
    public City(String city){
        this.city = city;
        this.documents = new LinkedHashSet<>();
    }

    /** constructor */
    public City(String city, String state, String population, String currency){
        this.city = city;
        this.state = state;
        this.population = population;
        this.currency = currency;
        this.documents = new LinkedHashSet<>();
    }

    /**
     * sets the members using api
     * @param api the api path to get the details from
     */
    public void setDetailsFromAPI(String api){
        getDetails(api);
    }

    /**
     *
     * @return the city name
     */
    public String getCity() {
        return city;
    }

    /**
     *
     * @return the currency used in the city
     */
    public String getCurrency() {
        return currency;
    }

    /**
     *
     * @return the size of population in the city
     */
    public String getPopulation() {
        return population;
    }

    /**
     *
     * @return the state of the city
     */
    public String getState() {
        return state;
    }

    /**
     *
     * @return the documents that are from the city or contains the city in text
     */
    public LinkedHashSet<Document> getDocuments() {
        return documents;
    }

    /**
     * setter
     * @param documents the documents that are from the city or contains the city in text
     */
    public void setDocuments(LinkedHashSet<Document> documents) {
        this.documents.addAll(documents);
    }

    /**
     * adds a document to the documents list
     * @param doc the document to add
     */
    public void addDocument(Document doc){
        documents.add(doc);
    }

    //use the api to get the details of the city
    private void getDetails(String api) {
        try {
            URL url = new URL(api + city);
            if (url != null) {

                String page = IOUtils.toString(url.openConnection().getInputStream());

                String curr = StringUtils.substringBetween(page, '"' + "geobytescurrencycode" + '"' + ":" + '"', '"' + ",");
                if (curr == null || curr.equals(""))
                    currency = "X";
                else
                    currency = curr;
                String pop = StringUtils.substringBetween(page, '"' + "geobytespopulation" + '"' + ":" + '"', '"' + ",");
                if (pop == null || pop.equals(""))
                    population = "X";
                else
                    population = getNumber(pop);
                String st = StringUtils.substringBetween(page, '"' + "geobytescountry" + '"' + ":" + '"', '"' + ",");
                if (st == null || st.equals(""))
                    state = "X";
                else
                    state = st;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //return the population size in a specific format
    private String getNumber(String s) {
        double num = Double.parseDouble(s);
        String Snum = "";
        if (num >= 1000000000) {
            num = num / 1000000000;
            num = DoubleRounder.round(num, 2);
            if (num == (int) num)
                Snum = (int) num + "B";
            else
                Snum = num + "B";
        } else if (num >= 1000000) {
            num = num / 1000000;
            num = DoubleRounder.round(num, 2);
            if (num == (int) num)
                Snum = (int) num + "M";
            else
                Snum = num + "M";
        } else if (num >= 1000) {
            num = num / 1000;
            num = DoubleRounder.round(num, 2);
            if (num == (int) num)
                Snum = (int) num + "K";
            else
                Snum = num + "K";
        } else
            Snum = s;
        return Snum;
    }
}
