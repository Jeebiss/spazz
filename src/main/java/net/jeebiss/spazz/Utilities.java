package net.jeebiss.spazz;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class Utilities {
    
    private static Random random = new Random();
    private static ArrayList<HashMap<Integer, Object>> quotes;
    
    @SuppressWarnings("unchecked")
    public static void loadQuotes() {
        try {
            quotes = new ArrayList<HashMap<Integer, Object>>();
            ArrayList<HashMap<Integer, Object>> map = null;
            Yaml yaml = new Yaml();
            File f = new File(System.getProperty("user.dir") + "/storage/quotes.yml");
            InputStream is = f.toURI().toURL().openStream();
            map = (ArrayList<HashMap<Integer, Object>>) yaml.load(is);
            for (int x = 0; x < map.size(); x++) {
                HashMap<Integer, Object> quote = map.get(x);
                for (int y = 0; y < quote.size(); y++) {
                    if (quote.get(y) instanceof byte[])
                        quote.put(y, new String((byte[]) quote.get(y)));
                }
                quotes.add(quote);
            }
        } catch(Exception e) {}
    }
    
    public static void saveQuotes() {
        try {
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(options);
            FileWriter writer = new FileWriter(System.getProperty("user.dir") + "/storage/quotes.yml");
            writer.write(yaml.dump(quotes));
            writer.close();
        } catch (Exception e) {}
    }
    
    public static boolean hasQuote(int number) {
        return quotes.get(number) != null;
    }
    
    public static int addQuote(String quote) {
        HashMap<Integer, Object> newQuote = new HashMap<Integer, Object>();
        newQuote.put(0, quote);
        quotes.add(newQuote);
        return quotes.size() - 1;
    }
    
    public static void addToQuote(int number, String quote) {
        quotes.get(number).put(quotes.get(number).size(), quote);
    }
    
    public static void removeQuote(int number) {
        quotes.remove(number);
    }
    
    public static HashMap<Integer, Object> getQuote(int number) {
        return quotes.get(number);
    }
    
    public static int getRandomNumber() {
        return random.nextInt();
    }
    
    public static int getRandomNumber(int limit) {
        return random.nextInt(limit);
    }
    
    public static int getQuoteCount() {
        return quotes.size();
    }

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
        Scanner s = new Scanner(is);
        s.useDelimiter("\\A");
        String r = s.hasNext() ? s.next() : "";
        s.close();
        return r;
    }
    
    @SuppressWarnings("unchecked")
    public static String getShortUrl(String url) {
        String shortened = null;
        try {
            JSONObject json = (JSONObject) JSONValue.parse(getStringFromUrl("https://api-ssl.bitly.com/v3/shorten?login=spazzmatic&apiKey=" 
                    + System.getProperty("spazz.bitly") + "&longUrl=" + url));
            shortened = (String) getJSONFromMap((Map<String, Object>) json.get("data")).get("url");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return shortened;
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
    
    public static String encodeBase64(String string) {
        String encoded = "";
        byte[] stringArray;
        try {
            stringArray = string.getBytes("UTF-8");
        } catch (Exception e) {
            stringArray = string.getBytes();
        }
        int paddingCount = (3 - (stringArray.length % 3)) % 3;
        stringArray = b64ZeroPad(stringArray.length + paddingCount, stringArray);
        for (int i = 0; i < stringArray.length; i += 3) {
            int j = ((stringArray[i] & 0xff) << 16) +
                ((stringArray[i + 1] & 0xff) << 8) + 
                (stringArray[i + 2] & 0xff);
            encoded = encoded + CS.charAt((j >> 18) & 0x3f) +
                CS.charAt((j >> 12) & 0x3f) +
                CS.charAt((j >> 6) & 0x3f) +
                CS.charAt(j & 0x3f);
        }
        return encoded + "==".substring(0, paddingCount);
    }
    
    private static byte[] b64ZeroPad(int length, byte[] bytes) {
        byte[] padded = new byte[length];
        System.arraycopy(bytes, 0, padded, 0, bytes.length);
        return padded;
    }
    
    private static final String CS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
    
}
