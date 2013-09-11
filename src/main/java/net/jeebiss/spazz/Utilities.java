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
        Scanner s = new Scanner(is);
        s.useDelimiter("\\A");
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
