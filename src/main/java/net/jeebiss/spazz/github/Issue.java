package net.jeebiss.spazz.github;

import net.jeebiss.spazz.util.Utilities;
import net.minidev.json.JSONObject;

import java.util.Date;
import java.util.Map;

public class Issue {

    private final GitHub root;

    private final Repository owner;
    private JSONObject information;

    public Issue(GitHub root, Repository owner, JSONObject information) {
        this.root = root;
        this.owner = owner;
        this.information = information;
    }

    public GitHub getGitHub() {
        return root;
    }

    @SuppressWarnings("unchecked")
    public User getUser() {
        JSONObject userInfo = Utilities.getJSONFromMap((Map<String, Object>) information.get("user"));
        return new User(root, userInfo);
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
        String state = ((String) information.get("state")).toLowerCase();
        return state;
    }

    public Date getLastUpdated() {
        return GitHub.parseDate((String) information.get("updated_at"));
    }

    public String getLastUpdatedSimple() {
        return GitHub.parseDateSimple((String) information.get("updated_at"));
    }

    @SuppressWarnings("unchecked")
    public String getUrl() {
        if (((Map<String, Object>) information.get("pull_request")).get("html_url") != null)
            return (String) ((Map<String, Object>) information.get("pull_request")).get("html_url");

        return (String) information.get("html_url");
    }

    @SuppressWarnings("unchecked")
    public boolean isPullRequest() {
        return (((Map<String, Object>) information.get("pull_request")).get("html_url") != null);
    }

    @SuppressWarnings("unchecked")
    public String getShortUrl() {
        if (((Map<String, Object>) information.get("pull_request")).get("html_url") != null)
            return Utilities.getShortUrl((String) ((Map<String, Object>) information.get("pull_request")).get("html_url"));

        return Utilities.getShortUrl((String) information.get("html_url"));
    }

    public Date getCreatedAt() {
        return GitHub.parseDate((String) information.get("created_at"));
    }

    public String getCreatedAtSimple() {
        return GitHub.parseDateSimple((String) information.get("created_at"));
    }

}
