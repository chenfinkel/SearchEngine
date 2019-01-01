package sample;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.decimal4j.util.DoubleRounder;

import java.net.URL;
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

    public void setDetailsFromAPI(String api){
        getDetails(api);
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

    public void addDocument(Document doc){
        documents.add(doc);
    }

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
