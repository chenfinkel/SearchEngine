package sample;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import okhttp3.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;

/**
 * A class to import data from the web = https://www.datamuse.com/api/
 */
public class WordSemantics {

    private JsonElement similarMeaning;
    private JsonElement triggered;
    OkHttpClient client;

    /** Constructor - Makes content with the websites data and uses Jason to parse it
     * @throws IOException In case the link is invalid
     */
    public WordSemantics()
    {
        client = new OkHttpClient();
    }

    private void setWord(String word)
    {
        try
        {
            JsonParser parser = new JsonParser();

            HttpUrl.Builder urlBuilder = HttpUrl.parse("https://api.datamuse.com/words?ml=" + word).newBuilder();
            String url = urlBuilder.build().toString();
            Request req = new Request.Builder().url(url).build();
            Response res = client.newCall(req).execute();
            similarMeaning = parser.parse(res.body().string());

            HttpUrl.Builder urlBuilder2 = HttpUrl.parse("https://api.datamuse.com/words?rel_trg=" + word).newBuilder();
            String url2 = urlBuilder2.build().toString();
            Request req2 = new Request.Builder().url(url2).build();
            Response res2 = client.newCall(req2).execute();
            triggered = parser.parse(res2.body().string());
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    /** if the word has semantics words, the function returns those words
     * @return semantics words
     */
    public HashSet<String> getSemanticsWords(String word)
    {
        setWord(word);
        HashSet<String> semanticsWords = new HashSet<>();
        JsonArray words = similarMeaning.getAsJsonArray();
        for (int i = 0; i < words.size() && i < 5; i++)
        {
            JsonObject objectWordDetails = (JsonObject) (words.get(i));
            String semanticWord = objectWordDetails.get("word").getAsString();
            semanticsWords.add(semanticWord);
        }
        JsonArray words2 = triggered.getAsJsonArray();
        for (int i = 0; i < words2.size() && i < 5; i++)
        {
            JsonObject objectWordDetails = (JsonObject) (words2.get(i));
            String semanticWord = objectWordDetails.get("word").getAsString();
            semanticsWords.add(semanticWord);
        }
        return semanticsWords;
    }

}