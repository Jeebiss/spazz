package net.jeebiss.spazz.github;

import java.util.Date;
import java.util.Map;

import net.jeebiss.spazz.Utilities;
import net.minidev.json.JSONObject;

public class Issue {

    private final GitHub root;
    
    private final Repository owner;
    private JSONObject information;
    
    public Issue(GitHub root, Repository owner, JSONObject information) {
        this.root = root;
        this.owner = owner;
        this.information = information;
    }
    
    @SuppressWarnings("unchecked")
    public User getUser() {
        JSONObject userInfo = Utilities.getJSONFromMap((Map<String, Object>) information.get("user"));
        return new User(root, (String) userInfo.get("login"), userInfo);
    }
    
    public int getNumber() {
        return (int) information.get("number");
    }
    
    public String getTitle() {
        return (String) information.get("title");
    }
    
    public String getBody() {
        return (String) information.get("body");
    }
    
    public Repository getRepo() {
        return owner;
    }
    
    public String getState() {
        return (String) information.get("state");
    }
    
    public Date getLastUpdated() {
        try {
            return GitHub.parseDate((String) information.get("updated_at"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public Date getCreatedAt() {
        try {
            return GitHub.parseDate((String) information.get("created_at"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
}
