package net.jeebiss.spazz.github;

import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import net.jeebiss.spazz.Utilities;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class Requester {
    
    private final GitHub root;
    
    private String method = "POST";
    public static Date lastChecked = null;
    
    public Requester(GitHub root) {
        this.root = root;
    }
    
    public Requester method(String method) {
        this.method = method;
        return this;
    }
    
    public JSONObject parse(String url) throws Exception {
        JSONObject json = new JSONObject();
        HttpURLConnection connection = setupConnection(url);
        if (connection.getResponseCode() == 304) {
            return json;
        }
        if (method.equals("GET")) {
            json = (JSONObject) JSONValue.parse(Utilities.getStringFromStream(connection.getInputStream()));
        }
        return json;
    }
    
    public JSONArray parseArray(String url) throws Exception {
        JSONArray json = new JSONArray();
        HttpURLConnection connection = setupConnection(url);
        if (connection.getResponseCode() == 304) {
            return json;
        }
        if (method.equals("GET")) {
            json = (JSONArray) JSONValue.parse(Utilities.getStringFromStream(connection.getInputStream()));
        }
        return json;
    }
    
    private HttpURLConnection setupConnection(String url) throws Exception {
        HttpURLConnection uc = (HttpURLConnection) new URL(url).openConnection();
        uc.setRequestProperty("Authorization", "Basic " + root.authentication);
        if (lastChecked != null) {
            uc.setRequestProperty("If-Modified-Since", new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz").format(lastChecked));
        }
        uc.setRequestMethod(method);
        return uc;
    }

}
