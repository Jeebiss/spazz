package net.jeebiss.spazz.github;

import java.util.Map;

import net.jeebiss.spazz.Utilities;
import net.minidev.json.JSONObject;

public class IssueComment implements Comment {

    private final GitHub root;
    
    private final Issue owner;
    private JSONObject information;
    
    public IssueComment(GitHub root, Issue owner, JSONObject information) {
        this.root = root;
        this.owner = owner;
        this.information = information;
    }
    
    public Issue getIssue() {
        return owner;
    }
    
    @Override
    public String getType() {
        return "Issue";
    }
    
    @Override
    public int getCommentId() {
        return (int) information.get("id");
    }

    @SuppressWarnings("unchecked")
    @Override
    public User getUser() {
        JSONObject userInfo = Utilities.getJSONFromMap((Map<String, Object>) information.get("user"));
        return new User(root, (String) userInfo.get("login"), userInfo);
    }
    
}
