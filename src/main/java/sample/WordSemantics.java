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
 * A class that connects to datamuse api to get semantic words
 */
public class WordSemantics {

    private JsonElement similarMeaning;
    private JsonElement triggered;
    OkHttpClient client;

    /** Constructor*/
    public WordSemantics()
    {
        client = new OkHttpClient();
    }

    //saves json elements conatining semantic words for a given word
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

    /** returns semantic words from json objects
     * @return semantics words
     */
    public HashSet<String> getSemanticsWords(String word)
    {
        setWord(word);
        HashSet<String> semanticsWords = new HashSet<>();
        JsonArray words = similarMeaning.getAsJsonArray();
        for (int i = 0; i < words.size() && i < 2; i++)
        {
            JsonObject objectWordDetails = (JsonObject) (words.get(i));
            String semanticWord = objectWordDetails.get("word").getAsString();
            semanticsWords.add(semanticWord);
        }
        JsonArray words2 = triggered.getAsJsonArray();
        for (int i = 0; i < words2.size() && i < 2; i++)
        {
            JsonObject objectWordDetails = (JsonObject) (words2.get(i));
            String semanticWord = objectWordDetails.get("word").getAsString();
            semanticsWords.add(semanticWord);
        }
        return semanticsWords;
    }

}