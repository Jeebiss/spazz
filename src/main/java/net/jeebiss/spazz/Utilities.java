package net.jeebiss.spazz;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import net.minidev.json.JSONObject;

public class Utilities {

    public static String getStringFromUrl(String url) throws IOException {
        return getStringFromStream(new URL(url).openStream());
    }
    
    public static String getStringFromUrls(List<String> urls) throws IOException {
        String returns = "";
        for (String url : urls) {
            returns += getStringFromUrl(url);
        }
        return returns;
    }
    
    public static String getStringFromStream(InputStream is) {
        Scanner s = new Scanner(is).useDelimiter("\\A");
        String r = s.hasNext() ? s.next() : "";
        s.close();
        return r;
    }
    
    public static String capitalize(String string) {
        String firstLetter = string.substring(0, 1);
        return string.replaceFirst(firstLetter, firstLetter.toUpperCase());
    }
    
    public static String uncapitalize(String string) {
        String firstLetter = string.substring(0, 1);
        return string.replaceFirst(firstLetter, firstLetter.toLowerCase());
    }
    
    public static JSONObject getJSONFromMap(Map<String, Object> map) {
        JSONObject json = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            json.put(entry.getKey(), entry.getValue());
        }
        return json;
    }
    
}
