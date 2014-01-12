package net.jeebiss.spazz.github;

import java.net.HttpURLConnection;
import java.net.URL;

import net.jeebiss.spazz.util.Utilities;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

public class Requester {
    
    private final GitHub root;
    
    private String method = "POST";
    
    public Requester(GitHub root) {
        this.root = root;
    }
    
    public Requester method(String method) {
        this.method = method;
        return this;
    }
    
    public JSONObject parse(String url) throws Exception {
        JSONObject json = null;
        if (method.equals("GET")) {
            json = (JSONObject) JSONValue.parse(Utilities.getStringFromStream(setupConnection(url).getInputStream()));
        }
        return json;
    }
    
    public JSONArray parseArray(String url) throws Exception {
        JSONArray json = null;
        if (method.equals("GET")) {
            json = (JSONArray) JSONValue.parse(Utilities.getStringFromStream(setupConnection(url).getInputStream()));
        }
        return json;
    }
    
    public HttpURLConnection githubConnection() throws Exception {
        return setupConnection(GitHub.GITHUB_URL);
    }
    
    private HttpURLConnection setupConnection(String url) throws Exception {
        URL loc = new URL(url);
        HttpURLConnection uc = (HttpURLConnection) loc.openConnection();
        uc.setRequestProperty("Authorization", "token " + root.authentication);
        uc.setRequestMethod(method);
        return uc;
    }

}
