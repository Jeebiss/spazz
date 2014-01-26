package net.jeebiss.spazz.github;

import net.jeebiss.spazz.util.Utilities;
import net.minidev.json.JSONObject;

import java.util.Map;

public class IssueComment implements Comment {

    private final GitHub root;

    private final Issue owner;
    private JSONObject information;

    public IssueComment(GitHub root, Issue owner, JSONObject information) {
        this.root = root;
        this.owner = owner;
        this.information = information;
    }

    public GitHub getGitHub() {
        return root;
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
        return new User(root, Utilities.getJSONFromMap((Map<String, Object>) information.get("user")));
    }

    @Override
    public String getUrl() {
        return (String) information.get("html_url");
    }

    @Override
    public String getShortUrl() {
        return Utilities.getShortUrl((String) information.get("html_url"));
    }

}
