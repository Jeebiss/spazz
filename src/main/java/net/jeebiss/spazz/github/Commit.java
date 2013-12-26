package net.jeebiss.spazz.github;

import java.util.Map;

import net.jeebiss.spazz.Utilities;
import net.minidev.json.JSONObject;

public class Commit {

    private final GitHub root;
    
    private final Repository owner;
    private JSONObject information;
    private JSONObject commitInfo;
    
    @SuppressWarnings("unchecked")
    public Commit(GitHub root, Repository owner, JSONObject information) {
        this.root = root;
        this.owner = owner;
        this.information = information;
        commitInfo = Utilities.getJSONFromMap((Map<String, Object>) information.get("commit"));
    }
    
    public GitHub getGitHub() {
        return root;
    }
    
    public String getCommitId() {
        return (String) information.get("sha");
    }
    
    public String getMessage() {
        return (String) commitInfo.get("message");
    }
    
    public boolean isPullRequest() {
        return (getMessage().startsWith("Merge pull request #"));
    }
    
    @SuppressWarnings("unchecked")
    public String getAuthor() {
        JSONObject userInfo = Utilities.getJSONFromMap((Map<String, Object>) commitInfo.get("author"));
        return (String) userInfo.get("name");
    }
    
    @SuppressWarnings("unchecked")
    public User getCommitter() {
        JSONObject userInfo = Utilities.getJSONFromMap((Map<String, Object>) information.get("committer"));
        return new User(root, userInfo);
    }
    
    public Repository getRepo() {
        return owner;
    }

    public String getUrl() {
        return (String) information.get("html_url");
    }

    public String getShortUrl() {
        return Utilities.getShortUrl((String) information.get("html_url"));
    }
    
}
