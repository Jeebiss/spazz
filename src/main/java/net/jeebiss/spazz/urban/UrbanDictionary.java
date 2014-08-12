package net.jeebiss.spazz.urban;

import com.google.gson.Gson;
import net.jeebiss.spazz.util.Utilities;

import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

public class UrbanDictionary {

    private static final Gson gson = new Gson();
    private static final String API_DEFINE = "http://api.urbandictionary.com/v0/define?term=";

    public static Response getDefinition(String input) {
        try {
            return gson.fromJson(Utilities.getStringFromStream(define(input)), Response.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static InputStream define(String input) throws Exception {
        return new URL(API_DEFINE + URLEncoder.encode(input, "UTF-8")).openConnection().getInputStream();
    }

}
