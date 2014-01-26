package net.jeebiss.spazz.github;

import net.jeebiss.spazz.util.Utilities;
import net.minidev.json.JSONObject;

import java.util.Map;

public class CommitComment implements Comment {

    private final GitHub root;

    private final Commit owner;
    private JSONObject information;

    public CommitComment(GitHub root, Commit owner, JSONObject information) {
        this.root = root;
        this.owner = owner;
        this.information = information;
    }

    public GitHub getGitHub() {
        return root;
    }

    public Commit getCommit() {
        return owner;
    }

    @Override
    public String getType() {
        return "Commit";
    }

    @Override
    public int getCommentId() {
        return (int) information.get("id");
    }

    @SuppressWarnings("unchecked")
    @Override
    public User getUser() {
        JSONObject userInfo = Utilities.getJSONFromMap((Map<String, Object>) information.get("user"));
        return new User(root, userInfo);
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
